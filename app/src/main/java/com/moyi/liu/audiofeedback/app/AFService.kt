package com.moyi.liu.audiofeedback.app

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.moyi.liu.audiofeedback.domain.AudioFeedbackHandler
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class AFService: Service() {

    @Inject
    lateinit var afHandler: AudioFeedbackHandler

    override fun onBind(intent: Intent?): IBinder? {
        Log.e("TEST", "onBind")
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.e("TEST", "onCreated")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e("TEST", "onStartCommand")
        afHandler.setup()
            .observeOn(Schedulers.io())
            .andThen(afHandler.calibrate())
            .subscribe(
                {

                },{
                    Log.e("TEST", it.message.orEmpty())
                }
            )
        return super.onStartCommand(intent, flags, startId)
    }
}