package com.example.rygg.feature.record.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.rygg.R
import com.example.rygg.core.notification.NotificationBuilder
import com.example.rygg.core.ui.utils.formatDistanceKm
import com.example.rygg.core.ui.utils.formatStopwatch
import com.example.rygg.feature.record.domain.RecordingSnapshot

// Rich foreground notification for an in-progress recording. A per-call strategy: content only —
// channel + small icon come from NotificationHelper's seeded base.
class RecordingNotificationBuilder(
    private val snapshot: RecordingSnapshot,
    private val pausePendingIntent: PendingIntent?,
    private val stopPendingIntent: PendingIntent?
) : NotificationBuilder {
    override fun build(context: Context, base: NotificationCompat.Builder): Notification {
        base.setContentTitle(context.getString(R.string.record_notification_title))
            .setContentText(
                context.getString(
                    R.string.record_notification_text,
                    formatDistanceKm(snapshot.distanceMeters),
                    formatStopwatch(snapshot.elapsedMillis)
                )
            )
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        pausePendingIntent?.let {
            base.addAction(android.R.drawable.ic_media_pause, context.getString(R.string.record_pause), it)
        }

        stopPendingIntent?.let {
            base.addAction(android.R.drawable.ic_media_play, context.getString(R.string.record_stop), it)
        }

        return base.build()
    }
}
