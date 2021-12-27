package com.moyi.liu.audiofeedback.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.moyi.liu.audiofeedback.app.AFService
import com.moyi.liu.audiofeedback.app.Action
import com.moyi.liu.audiofeedback.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.clickButton.setOnClickListener {
            startForegroundService(
                Intent(this@MainActivity, AFService::class.java)
                    .apply { action = Action.START_FOREGROUND_SERVICE }
            )
        }
    }

    override fun onDestroy() {
        startService(
            Intent(this@MainActivity, AFService::class.java)
                .apply {
                    action = Action.STOP_FOREGROUND_SERVICE
                }
        )
        super.onDestroy()
    }
}