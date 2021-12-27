package com.moyi.liu.audiofeedback.app

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import com.moyi.liu.audiofeedback.app.Notification.DEFAULT_CHANNEL_ID
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AFApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        //TODO add to string res
        val channel = NotificationChannel(
            DEFAULT_CHANNEL_ID,
            "Audio-Biofeedback Service",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            lightColor = Color.BLUE
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(channel)
    }
}