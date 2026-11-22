package com.dimitrijeilic.rygg.feature.offline.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import com.dimitrijeilic.rygg.R
import com.dimitrijeilic.rygg.core.notification.NotificationBuilder

class OfflineDownloadNotificationBuilder(
    private val regionName: String,
    private val progress: Float,
    private val sizeText: String,
    private val contentPendingIntent: PendingIntent?,
    private val pausePendingIntent: PendingIntent?
) : NotificationBuilder {
    override fun build(context: Context, base: NotificationCompat.Builder): Notification {
        val percent = (progress * PERCENT).toInt()
        base.setContentTitle(context.getString(R.string.offline_notification_title, regionName))
            .setContentText(context.getString(R.string.offline_notification_progress, percent, sizeText))
            .setProgress(PERCENT, percent, percent == 0)
            .setContentIntent(contentPendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        pausePendingIntent?.let {
            base.addAction(0, context.getString(R.string.offline_notification_pause), it)
        }
        return base.build()
    }

    private companion object {
        const val PERCENT = 100
    }
}
