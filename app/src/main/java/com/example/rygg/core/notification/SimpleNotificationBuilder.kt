package com.example.rygg.core.notification

import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationCompat

// Plain title/message notification — the everyday case that used to live inline in
// NotificationHelper.
class SimpleNotificationBuilder(
    private val title: String,
    private val message: String
) : NotificationBuilder {
    override fun build(context: Context, base: NotificationCompat.Builder): Notification =
        base.setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
}
