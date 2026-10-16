package com.dimitrijeilic.rygg.feature.map.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import com.dimitrijeilic.rygg.R
import com.dimitrijeilic.rygg.core.notification.NotificationBuilder

class FollowingNotificationBuilder(
    private val routeName: String,
    private val isOffRoute: Boolean,
    private val contentPendingIntent: PendingIntent?,
    private val stopPendingIntent: PendingIntent?
) : NotificationBuilder {
    override fun build(context: Context, base: NotificationCompat.Builder): Notification {
        base.setContentTitle(context.getString(R.string.follow_notification_title, routeName))
            .setContentText(context.getString(if (isOffRoute) R.string.follow_off_route else R.string.follow_on_route))
            .setContentIntent(contentPendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        stopPendingIntent?.let {
            base.addAction(0, context.getString(R.string.follow_stop), it)
        }
        return base.build()
    }
}
