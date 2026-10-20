package com.dimitrijeilic.rygg.core.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.dimitrijeilic.rygg.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * The single funnel for notifications: owns the channels, the small-icon seed and the
 * POST_NOTIFICATIONS gate, and renders any [NotificationBuilder] strategy. Inject anywhere
 * (ViewModel, repository, service, …). On API 33+ posting is a safe no-op without the
 * permission.
 */
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Render a strategy to a finished Notification (e.g. for Service.startForeground).
    fun build(builder: NotificationBuilder, channel: RyggChannel = RyggChannel.GENERAL): Notification {
        ensureChannel(channel)
        return builder.build(context, seededBase(channel))
    }

    fun notify(
        builder: NotificationBuilder,
        notificationId: Int = DEFAULT_NOTIFICATION_ID,
        channel: RyggChannel = RyggChannel.GENERAL
    ) {
        if (!canPost(channel)) return
        @Suppress("MissingPermission")
        NotificationManagerCompat.from(context).notify(notificationId, build(builder, channel))
    }

    fun notify(title: String, message: String, notificationId: Int = DEFAULT_NOTIFICATION_ID) =
        notify(SimpleNotificationBuilder(title, message), notificationId)

    fun cancel(notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }

    fun canPost(channel: RyggChannel = RyggChannel.GENERAL): Boolean {
        if (!hasPostPermission()) return false
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ensureChannel(channel)
            val importance = manager.getNotificationChannel(channel.id)?.importance
            if (importance == NotificationManager.IMPORTANCE_NONE) return false
        }
        return true
    }

    private fun seededBase(channel: RyggChannel): NotificationCompat.Builder =
        NotificationCompat.Builder(context, channel.id).setSmallIcon(R.drawable.ic_logo)

    private fun ensureChannel(channel: RyggChannel) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        val notificationChannel = when (channel) {
            RyggChannel.GENERAL -> NotificationChannel(
                channel.id,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )

            RyggChannel.OFF_ROUTE -> NotificationChannel(
                channel.id,
                context.getString(R.string.follow_alert_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.follow_alert_channel_description)
                enableVibration(true)
                vibrationPattern = OFF_ROUTE_VIBRATION_PATTERN
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }
        }
        manager.createNotificationChannel(notificationChannel)
    }

    private fun hasPostPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }
}

enum class RyggChannel(val id: String) {
    GENERAL(CHANNEL_ID),
    OFF_ROUTE("off_route")
}

const val CHANNEL_ID = "general"
const val DEFAULT_NOTIFICATION_ID = 1001
const val FOLLOW_NOTIFICATION_ID = 1002
const val OFF_ROUTE_NOTIFICATION_ID = 1003

val OFF_ROUTE_VIBRATION_PATTERN = longArrayOf(0, 400, 200, 400, 200, 400)
