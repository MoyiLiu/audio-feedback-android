package com.moyi.liu.audiofeedback.audio

import com.moyi.liu.audiofeedback.transformer.MIN_VOLUME
import com.moyi.liu.audiofeedback.transformer.NORMAL_PLAY_RATE

/**
 * @param volume range 0.0 to 1.0
 * @param playRate range 1.0 to 2.0
 */
data class AudioContext(
    val volume: Float,
    val playRate: Float
) {
    companion object {
        val MUTE = AudioContext(MIN_VOLUME, NORMAL_PLAY_RATE)
    }
}



