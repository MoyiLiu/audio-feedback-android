package com.moyi.liu.audiofeedback.transformer

import com.moyi.liu.audiofeedback.audio.AudioContext
import com.moyi.liu.audiofeedback.sway.Boundary
import kotlin.math.pow

class SensorDataTransformer(
    private val frontBackAxisInitialValue: Float,
    private val frontBackBoundaries: Pair<Boundary, Boundary> //<Front, Back>
) {
    /**
     * @param sensorData x,y,z axes values, refer to [https://developer.android.com/reference/android/hardware/SensorEvent]
     * @return a pair of Font and Back [AudioContext]
     */
    fun transformForFrontBackTracks(sensorData: Triple<Float, Float, Float>): Pair<AudioContext, AudioContext> {
        val (_, _, value) = sensorData
        val (frontBoundary, backBoundary) = frontBackBoundaries
        // value > frontBackAxisInitialValue ==> back
        // value <= frontBackAxisInitialValue ==> front
        return when {
            value > frontBackAxisInitialValue ->
                AudioContext.MUTE to value.transformToPlayRateAudioContext(backBoundary)
            else ->
                value.transformToLoudnessAudioContext(frontBoundary) to AudioContext.MUTE
        }
    }
}

fun Float.transformToLoudnessAudioContext(boundary: Boundary): AudioContext {
    val (min, max) = boundary
    return when {
        this < min -> AudioContext(MIN_VOLUME, NORMAL_PLAY_RATE)
        this >= max -> AudioContext(MAX_VOLUME, NORMAL_PLAY_RATE)
        else -> AudioContext(
            ((this - min) / (max - min)).pow(2),
            NORMAL_PLAY_RATE
        )
    }
}

fun Float.transformToPlayRateAudioContext(boundary: Boundary): AudioContext {
    val (min, max) = boundary
    return when {
        this < min -> AudioContext(MIN_VOLUME, NORMAL_PLAY_RATE)
        this >= max -> AudioContext(MAX_VOLUME, MAX_PLAY_RATE)
        else -> AudioContext(
            MAX_VOLUME,
            NORMAL_PLAY_RATE + ((this - min) / (max - min)).pow(2)
        )
    }
}