package com.dimitrijeilic.rygg.feature.map.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import com.dimitrijeilic.rygg.R
import com.dimitrijeilic.rygg.core.notification.NotificationBuilder
import com.dimitrijeilic.rygg.core.notification.OFF_ROUTE_VIBRATION_PATTERN

class OffRouteAlertBuilder(
    private val distanceText: String,
    private val contentPendingIntent: PendingIntent?,
    private val snoozePendingIntent: PendingIntent?
) : NotificationBuilder {
    override fun build(context: Context, base: NotificationCompat.Builder): Notification {
        base.setContentTitle(context.getString(R.string.follow_alert_title))
            .setContentText(context.getString(R.string.follow_alert_text, distanceText))
            .setContentIntent(contentPendingIntent)
            .setCategory(NotificationCompat.CATEGORY_NAVIGATION)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_SOUND)
            .setVibrate(OFF_ROUTE_VIBRATION_PATTERN)
            .setAutoCancel(true)

        snoozePendingIntent?.let {
            base.addAction(0, context.getString(R.string.follow_alert_snooze), it)
        }
        return base.build()
    }
}
