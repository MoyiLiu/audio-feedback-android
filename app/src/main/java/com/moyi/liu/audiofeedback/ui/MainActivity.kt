package com.moyi.liu.audiofeedback.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.moyi.liu.audiofeedback.R
import com.moyi.liu.audiofeedback.app.AFService
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
            startForegroundService(Intent(this@MainActivity, AFService::class.java))
        }
    }
}