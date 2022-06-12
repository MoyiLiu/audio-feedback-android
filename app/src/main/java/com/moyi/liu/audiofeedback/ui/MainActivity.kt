package com.moyi.liu.audiofeedback.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.moyi.liu.audiofeedback.app.AFService
import com.moyi.liu.audiofeedback.app.Action
import com.moyi.liu.audiofeedback.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mService: AFService
    private var isBounded = false
    private val serviceIntent get() = Intent(this, AFService::class.java)

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as AFService.AFServiceBinder
            mService = binder.service
            isBounded = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBounded = false
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.startButton.setOnClickListener {
            val intent = serviceIntent.apply { action = Action.START_FOREGROUND_SERVICE }
            startForegroundService(intent)
            bindService(serviceIntent, connection, Context.BIND_IMPORTANT)
        }

        binding.clickButton.setOnClickListener {
            if (isBounded) {
                val num: Int = mService.randomNumber
                Toast.makeText(this, "number: $num", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        unbindService(connection)
        startService(
            serviceIntent.apply { action = Action.STOP_FOREGROUND_SERVICE }
        )
        super.onDestroy()
    }
}