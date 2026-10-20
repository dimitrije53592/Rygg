package com.dimitrijeilic.rygg.feature.record.service

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.dimitrijeilic.rygg.core.MainActivity
import com.dimitrijeilic.rygg.core.navigation.InternalDeepLinks
import com.dimitrijeilic.rygg.core.notification.DEFAULT_NOTIFICATION_ID
import com.dimitrijeilic.rygg.core.notification.NotificationHelper
import com.dimitrijeilic.rygg.core.units.MeasurementSystem
import com.dimitrijeilic.rygg.feature.auth.domain.Discipline
import com.dimitrijeilic.rygg.feature.record.data.RecordingController
import com.dimitrijeilic.rygg.feature.record.domain.RecordingSnapshot
import com.dimitrijeilic.rygg.feature.record.domain.RecordingState
import com.dimitrijeilic.rygg.feature.settings.data.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

// Thin Android shell: routes control intents to the controller, owns the foreground state, and
// mirrors the controller's snapshot into the ongoing notification. No GPS or timing logic here.
@AndroidEntryPoint
class RecordingService : Service() {
    @Inject
    lateinit var controller: RecordingController

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var isForeground = false
    private var measurementSystem = MeasurementSystem.METRIC

    override fun onCreate() {
        super.onCreate()
        serviceScope.launch {
            combine(controller.snapshot, settingsRepository.measurementSystem) { snapshot, system ->
                measurementSystem = system
                snapshot
            }.collect { snapshot ->
                if (isForeground) {
                    notificationHelper.notify(notificationFor(snapshot), DEFAULT_NOTIFICATION_ID)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val discipline = intent.getStringExtra(EXTRA_DISCIPLINE)
                    ?.let { runCatching { Discipline.valueOf(it) }.getOrNull() }
                    ?: Discipline.HIKE
                // Promote to foreground before the controller starts collecting location.
                startForegroundRecording()
                controller.start(discipline)
            }

            ACTION_PAUSE -> controller.pause()

            ACTION_RESUME -> controller.resume()

            ACTION_STOP -> {
                controller.stop()
                stopForegroundAndSelf()
            }

            ACTION_RECOVER -> recover(alwaysResume = true)

            // Null intent: the system restarted us after killing the process mid-recording.
            null -> recover(alwaysResume = false)
        }
        // Sticky so a killed recording is restarted and rebuilt from the journal.
        return START_STICKY
    }

    private fun recover(alwaysResume: Boolean) {
        // Android 12+ may refuse a foreground start from the background; the journal is kept, and
        // the app offers to recover on next launch instead.
        if (!tryStartForeground()) {
            stopSelf()
            return
        }
        serviceScope.launch {
            val recovered = controller.recover()
            when {
                recovered == null && controller.isIdle() -> stopForegroundAndSelf()
                recovered == null -> Unit
                alwaysResume || recovered.state != RecordingState.PAUSED -> controller.resume()
            }
        }
    }

    private fun tryStartForeground(): Boolean =
        try {
            startForegroundRecording()
            true
        } catch (e: IllegalStateException) {
            false
        } catch (e: SecurityException) {
            false
        }

    private fun stopForegroundAndSelf() {
        isForeground = false
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startForegroundRecording() {
        val notification = notificationHelper.build(notificationFor(controller.snapshot.value))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                DEFAULT_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            ServiceCompat.startForeground(this, DEFAULT_NOTIFICATION_ID, notification, 0)
        }
        isForeground = true
    }

    private fun notificationFor(snapshot: RecordingSnapshot): RecordingNotificationBuilder =
        RecordingNotificationBuilder(
            snapshot = snapshot,
            measurementSystem = measurementSystem,
            contentPendingIntent = getContentPendingIntent(),
            pausePendingIntent = getPauseOrResumePendingIntent(
                snapshot.state == RecordingState.PAUSED || snapshot.state == RecordingState.AUTO_PAUSED
            ),
            stopPendingIntent = getStopPendingIntent()
        )

    private fun getPauseOrResumePendingIntent(isPaused: Boolean): PendingIntent {
        val action = if (isPaused) ACTION_RESUME else ACTION_PAUSE
        val intent = Intent(this, RecordingService::class.java).apply {
            this.action = action
        }
        val requestCode = if (isPaused) REQUEST_CODE_RESUME else REQUEST_CODE_PAUSE
        return PendingIntent.getService(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    // Tapping the body opens the live recording screen; recording keeps running.
    private fun getContentPendingIntent(): PendingIntent =
        deepLinkActivityPendingIntent(REQUEST_CODE_CONTENT, InternalDeepLinks.RECORD)

    // Stop opens the save/preview screen; the recording is stopped on arrival (AppNavigation's
    // deep-link side effects + RecordingPreviewViewModel).
    private fun getStopPendingIntent(): PendingIntent =
        deepLinkActivityPendingIntent(REQUEST_CODE_STOP, InternalDeepLinks.RECORDING_PREVIEW)

    // Activity PendingIntent, not getService: Android 12+ bans notification -> service -> activity
    // trampolines, so notification navigation must target MainActivity directly via a deep link.
    private fun deepLinkActivityPendingIntent(requestCode: Int, deepLink: String): PendingIntent {
        val intent = Intent(Intent.ACTION_VIEW, deepLink.toUri(), this, MainActivity::class.java)
        return PendingIntent.getActivity(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "com.dimitrijeilic.rygg.record.START"
        const val ACTION_PAUSE = "com.dimitrijeilic.rygg.record.PAUSE"
        const val ACTION_RESUME = "com.dimitrijeilic.rygg.record.RESUME"
        const val ACTION_STOP = "com.dimitrijeilic.rygg.record.STOP"
        const val ACTION_RECOVER = "com.dimitrijeilic.rygg.record.RECOVER"
        const val EXTRA_DISCIPLINE = "discipline"

        private const val REQUEST_CODE_PAUSE = 100
        private const val REQUEST_CODE_RESUME = 101
        private const val REQUEST_CODE_STOP = 102
        private const val REQUEST_CODE_CONTENT = 103

        fun start(context: Context, discipline: Discipline) =
            send(context, ACTION_START, foreground = true) { putExtra(EXTRA_DISCIPLINE, discipline.name) }

        fun pause(context: Context) = send(context, ACTION_PAUSE)

        fun resume(context: Context) = send(context, ACTION_RESUME)

        fun stop(context: Context) = send(context, ACTION_STOP)

        fun recover(context: Context) = send(context, ACTION_RECOVER, foreground = true)

        // Only START promotes the service to the foreground. Control actions target the already
        // running service via startService, so they don't owe the system a startForeground()
        // call within the timeout (which STOP could never honor). See review finding #1.
        private inline fun send(
            context: Context,
            action: String,
            foreground: Boolean = false,
            extras: Intent.() -> Unit = {}
        ) {
            val intent = Intent(context, RecordingService::class.java).apply {
                this.action = action
                extras()
            }
            if (foreground) {
                ContextCompat.startForegroundService(context, intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
