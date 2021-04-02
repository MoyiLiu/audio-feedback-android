package com.moyi.liu.audiofeedback.transformer

import com.moyi.liu.audiofeedback.audio.AudioContext
import com.moyi.liu.audiofeedback.domain.model.Boundary
import kotlin.math.*

class SensorDataTransformer(
    private val frontBackAxisOriginValue: Float,
    frontBackBoundaries: Pair<Boundary, Boundary>//<Front, Back>
) {

    private val frontBoundary: Boundary = frontBackBoundaries.first
    private val backBoundary: Boundary = frontBackBoundaries.first

    /**
     * @param axisSensorValue front-back axis sensor value, refer to [https://developer.android.com/reference/android/hardware/SensorEvent]
     * @return a pair of Font and Back [AudioContext]
     */
    fun transformForFrontBackTracks(axisSensorValue: Float): Pair<AudioContext, AudioContext> {
        // axisSensorValue > frontBackAxisInitialValue ==> front
        // axisSensorValue <= frontBackAxisInitialValue ==> back
        val angle = abs(axisSensorValue - frontBackAxisOriginValue).gravitySensorValueToAngle()
        return when {
            axisSensorValue > frontBackAxisOriginValue ->
                angle.transformToLoudnessAudioContext(frontBoundary) to AudioContext.MUTE
            else ->
                AudioContext.MUTE to angle.transformToPlayRateAudioContext(backBoundary)
        }
    }
}

/**
 * Transform sway angle to loudness driven AudioContext
 * - < min => mute
 * - Angle increases > min & <= max => loudness increases
 * - > max => Max volume
 */
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

/**
 * Transform sway angle to play rate driven AudioContext.
 * - < min => mute
 * - Angle increases > min & <= max => play rate increases
 * - > max => Max volume and Max play rate
 */
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

fun Float.gravitySensorValueToAngle(): Float =
    when {
        abs(this) > MAX_GRAVITY_SENSOR_VALUE -> MAX_GRAVITY_SENSOR_VALUE
        else -> abs(this)
    }.let { sensorValue ->
        asin(sensorValue / MAX_GRAVITY_SENSOR_VALUE) * 180 / PI.toFloat()
    }

fun Float.angleToGravitySensorValue(): Float =
    sin(this * PI.toFloat() / 180) * MAX_GRAVITY_SENSOR_VALUE