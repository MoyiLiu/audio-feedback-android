package com.moyi.liu.audiofeedback.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Icon
import android.os.IBinder
import android.util.Log
import com.moyi.liu.audiofeedback.R
import com.moyi.liu.audiofeedback.domain.AudioFeedbackHandler
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@AndroidEntryPoint
class AFService : Service() {

    @Inject
    lateinit var afHandler: AudioFeedbackHandler

    private var isStarted = false

    override fun onBind(intent: Intent?): IBinder? {
        Log.e("TEST", "onBind")
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //TODO refactor
        if (!isStarted && intent?.action == Action.START_FOREGROUND_SERVICE) {
            Log.e("TEST", "Start service")
            isStarted = true
            val notification = Notification.Builder(this, "Default")
                .setContentTitle("Notification")
                .setContentText("Running")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setLargeIcon(Icon.createWithResource(this, R.drawable.ic_launcher_foreground))
                .build()
            startForeground(1, notification)

            afHandler.setup()
                .observeOn(Schedulers.io())
                .andThen(afHandler.calibrate())
                .andThen(afHandler.start())
                .subscribe(
                    {

                    }, {
                        Log.e("TEST", it.message.orEmpty())
                    }
                )
        } else if (intent?.action == Action.STOP_FOREGROUND_SERVICE) {
            afHandler.terminate()
            Log.e("TEST", "Stop service")
            stopForeground(true)
            stopSelf()
            isStarted = false
        }
        return START_NOT_STICKY
    }
}