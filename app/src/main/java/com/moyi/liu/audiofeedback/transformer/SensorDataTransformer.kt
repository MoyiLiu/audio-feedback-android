package com.moyi.liu.audiofeedback.transformer

import com.moyi.liu.audiofeedback.audio.AudioContext
import com.moyi.liu.audiofeedback.sway.Boundary
import kotlin.math.pow

fun Float.transformToLoudnessAudioContext(boundary: Boundary): AudioContext {
    val (min, max) = boundary
    return when {
        this < min -> AudioContext(0f, 1f)
        this >= max -> AudioContext(1f, 1f)
        else -> AudioContext(
            ((this - min) / (max - min)).pow(2),
            1f
        )
    }
}
