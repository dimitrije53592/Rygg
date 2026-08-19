package com.example.rygg.core.notification

import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationCompat

// Strategy: shape content onto the shared, channel-seeded base builder that NotificationHelper
// hands in. Keeps channel/icon/id/permission concerns out of individual notifications.
fun interface NotificationBuilder {
    fun build(context: Context, base: NotificationCompat.Builder): Notification
}
