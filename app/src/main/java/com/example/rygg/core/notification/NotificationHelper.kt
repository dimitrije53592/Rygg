package com.example.rygg.core.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.rygg.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * The single funnel for notifications: owns the shared channel, the small-icon seed and the
 * POST_NOTIFICATIONS gate, and renders any [NotificationBuilder] strategy. Inject anywhere
 * (ViewModel, repository, service, …). On API 33+ posting is a safe no-op without the
 * permission.
 */
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Render a strategy to a finished Notification (e.g. for Service.startForeground).
    fun build(builder: NotificationBuilder): Notification {
        ensureChannel()
        return builder.build(context, seededBase())
    }

    fun notify(builder: NotificationBuilder, notificationId: Int = DEFAULT_NOTIFICATION_ID) {
        if (!hasPostPermission()) return
        @Suppress("MissingPermission")
        NotificationManagerCompat.from(context).notify(notificationId, build(builder))
    }

    fun notify(title: String, message: String, notificationId: Int = DEFAULT_NOTIFICATION_ID) =
        notify(SimpleNotificationBuilder(title, message), notificationId)

    private fun seededBase(): NotificationCompat.Builder =
        NotificationCompat.Builder(context, CHANNEL_ID).setSmallIcon(R.drawable.ic_logo)

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun hasPostPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }
}

const val CHANNEL_ID = "general"
const val DEFAULT_NOTIFICATION_ID = 1001
