package com.example.dynamic.notifications

import android.content.Context
import androidx.annotation.Keep
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.NotificationUpdater
import com.google.android.play.core.splitcompat.SplitCompat

@Keep
class NotificationUpdaterImpl: NotificationUpdater {
    override fun showNotification(context: Context) {
        SplitCompat.install(context)
        val builder =
            NotificationCompat.Builder(context, MainActivity.NOTIFICATION_CHANNEL_ID)
        val notification = builder
            .setContentTitle("Feature notification")
            .setSmallIcon(R.drawable.feature_icon)
            .build()
        NotificationManagerCompat.from(context).notify(2, notification)
    }
}