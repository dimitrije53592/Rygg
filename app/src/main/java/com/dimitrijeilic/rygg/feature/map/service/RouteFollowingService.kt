package com.dimitrijeilic.rygg.feature.map.service

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.dimitrijeilic.rygg.core.notification.FOLLOW_NOTIFICATION_ID
import com.dimitrijeilic.rygg.core.notification.NotificationHelper
import com.dimitrijeilic.rygg.core.notification.OFF_ROUTE_NOTIFICATION_ID
import com.dimitrijeilic.rygg.core.notification.OFF_ROUTE_VIBRATION_PATTERN
import com.dimitrijeilic.rygg.core.notification.RyggChannel
import com.dimitrijeilic.rygg.core.units.UnitFormatter
import com.dimitrijeilic.rygg.feature.map.data.RouteFollowingController
import com.dimitrijeilic.rygg.feature.map.domain.FollowSnapshot
import com.dimitrijeilic.rygg.feature.settings.data.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RouteFollowingService : Service() {
    @Inject
    lateinit var controller: RouteFollowingController

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var isForeground = false
    private var alertedEpisode = 0

    override fun onCreate() {
        super.onCreate()
        serviceScope.launch {
            controller.snapshot
                .distinctUntilChangedBy { it.isActive to it.isOffRoute }
                .collect { snapshot ->
                    when {
                        !isForeground -> Unit
                        !snapshot.isActive -> stopFollowing()
                        else -> notificationHelper.notify(ongoingNotification(snapshot), FOLLOW_NOTIFICATION_ID)
                    }
                }
        }
        serviceScope.launch {
            combine(controller.snapshot, settingsRepository.offRouteAlertsEnabled) { snapshot, enabled ->
                snapshot to enabled
            }.collect { (snapshot, enabled) -> handleAlert(snapshot, enabled) }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                startForegroundFollowing()
                if (!controller.snapshot.value.isActive) stopFollowing()
            }

            ACTION_SNOOZE -> {
                controller.snooze()
                notificationHelper.cancel(OFF_ROUTE_NOTIFICATION_ID)
            }

            ACTION_STOP -> {
                controller.stop()
                stopFollowing()
            }
        }
        return START_NOT_STICKY
    }

    private fun handleAlert(snapshot: FollowSnapshot, alertsEnabled: Boolean) {
        if (!snapshot.isActive || !snapshot.isOffRoute) {
            notificationHelper.cancel(OFF_ROUTE_NOTIFICATION_ID)
            return
        }
        if (snapshot.offRouteEpisode == alertedEpisode) return
        alertedEpisode = snapshot.offRouteEpisode
        if (!alertsEnabled || controller.isSnoozed()) return

        if (notificationHelper.canPost(RyggChannel.OFF_ROUTE)) {
            serviceScope.launch {
                val units = UnitFormatter(resources, settingsRepository.currentMeasurementSystem())
                notificationHelper.notify(
                    builder = OffRouteAlertBuilder(
                        distanceText = units.distance(snapshot.distanceToRouteMeters ?: 0.0),
                        contentPendingIntent = contentPendingIntent(),
                        snoozePendingIntent = servicePendingIntent(ACTION_SNOOZE, REQUEST_CODE_SNOOZE)
                    ),
                    notificationId = OFF_ROUTE_NOTIFICATION_ID,
                    channel = RyggChannel.OFF_ROUTE
                )
            }
        } else {
            vibrate()
        }
    }

    private fun startForegroundFollowing() {
        val notification = notificationHelper.build(ongoingNotification(controller.snapshot.value))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                FOLLOW_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            ServiceCompat.startForeground(this, FOLLOW_NOTIFICATION_ID, notification, 0)
        }
        isForeground = true
    }

    private fun stopFollowing() {
        notificationHelper.cancel(OFF_ROUTE_NOTIFICATION_ID)
        isForeground = false
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun ongoingNotification(snapshot: FollowSnapshot) = FollowingNotificationBuilder(
        routeName = snapshot.routeName,
        isOffRoute = snapshot.isOffRoute,
        contentPendingIntent = contentPendingIntent(),
        stopPendingIntent = servicePendingIntent(ACTION_STOP, REQUEST_CODE_STOP)
    )

    private fun contentPendingIntent(): PendingIntent? {
        val intent = packageManager.getLaunchIntentForPackage(packageName) ?: return null
        return PendingIntent.getActivity(
            this,
            REQUEST_CODE_CONTENT,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun servicePendingIntent(action: String, requestCode: Int): PendingIntent =
        PendingIntent.getService(
            this,
            requestCode,
            Intent(this, RouteFollowingService::class.java).setAction(action),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun vibrate() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Vibrator::class.java)
        }
        vibrator?.vibrate(VibrationEffect.createWaveform(OFF_ROUTE_VIBRATION_PATTERN, -1))
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "com.dimitrijeilic.rygg.follow.START"
        const val ACTION_SNOOZE = "com.dimitrijeilic.rygg.follow.SNOOZE"
        const val ACTION_STOP = "com.dimitrijeilic.rygg.follow.STOP"

        private const val REQUEST_CODE_STOP = 200
        private const val REQUEST_CODE_SNOOZE = 201
        private const val REQUEST_CODE_CONTENT = 202

        fun start(context: Context) {
            val intent = Intent(context, RouteFollowingService::class.java).setAction(ACTION_START)
            ContextCompat.startForegroundService(context, intent)
        }
    }
}
