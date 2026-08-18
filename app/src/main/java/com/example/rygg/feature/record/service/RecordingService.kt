package com.example.rygg.feature.record.service

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.example.rygg.core.notification.DEFAULT_NOTIFICATION_ID
import com.example.rygg.core.notification.NotificationHelper
import com.example.rygg.feature.auth.domain.Discipline
import com.example.rygg.feature.record.data.RecordingController
import com.example.rygg.feature.record.domain.RecordingSnapshot
import com.example.rygg.feature.record.domain.RecordingState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
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

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var isForeground = false

    override fun onCreate() {
        super.onCreate()
        serviceScope.launch {
            controller.snapshot.collect { snapshot ->
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
                isForeground = false
                ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        // Not sticky: after a process kill the in-memory recording is gone, so a null-intent
        // redelivery would only resurrect a service that never re-enters the foreground.
        return START_NOT_STICKY
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
            pausePendingIntent = getPauseOrResumePendingIntent(snapshot.state == RecordingState.PAUSED),
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

    private fun getStopPendingIntent(): PendingIntent {
        val intent = Intent(this, RecordingService::class.java).apply {
            action = ACTION_STOP
        }
        return PendingIntent.getService(
            this,
            REQUEST_CODE_STOP,
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
        const val ACTION_START = "com.example.rygg.record.START"
        const val ACTION_PAUSE = "com.example.rygg.record.PAUSE"
        const val ACTION_RESUME = "com.example.rygg.record.RESUME"
        const val ACTION_STOP = "com.example.rygg.record.STOP"
        const val EXTRA_DISCIPLINE = "discipline"

        private const val REQUEST_CODE_PAUSE = 100
        private const val REQUEST_CODE_RESUME = 101
        private const val REQUEST_CODE_STOP = 102

        fun start(context: Context, discipline: Discipline) =
            send(context, ACTION_START, foreground = true) { putExtra(EXTRA_DISCIPLINE, discipline.name) }

        fun pause(context: Context) = send(context, ACTION_PAUSE)

        fun resume(context: Context) = send(context, ACTION_RESUME)

        fun stop(context: Context) = send(context, ACTION_STOP)

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
