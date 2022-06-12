package com.moyi.liu.audiofeedback.app

import android.app.Notification
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.moyi.liu.audiofeedback.R
import com.moyi.liu.audiofeedback.domain.AudioFeedbackHandler
import com.moyi.liu.audiofeedback.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class AFService : Service() {

    @Inject
    lateinit var afHandler: AudioFeedbackHandler

    private val binder = AFServiceBinder()

    private var isStarted = false

    // Random number generator
    private val mGenerator = Random()

    /** method for clients  */
    val randomNumber: Int
        get() = mGenerator.nextInt(100)

    override fun onBind(intent: Intent?): IBinder {
        Log.e("TEST", "onBind")
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //TODO refactor
        if (!isStarted && intent?.action == Action.START_FOREGROUND_SERVICE) {
            Log.e("TEST", "Start service")
            isStarted = true
            val pendingIntent: PendingIntent =
                Intent(this, MainActivity::class.java).let { notificationIntent ->
                    PendingIntent.getActivity(this, 0, notificationIntent, FLAG_IMMUTABLE)
                }
            val notification = Notification.Builder(this, "Default")
                .setContentTitle("Notification")
                .setContentText("Running")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
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

    /**
     * **See Also:** [Bound Service Doc](https://developer.android.com/guide/components/bound-services)
     */
    inner class AFServiceBinder : Binder() {
        val service get() = this@AFService
    }
}