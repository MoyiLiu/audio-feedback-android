package com.moyi.liu.audiofeedback.transformer

import com.moyi.liu.audiofeedback.audio.AudioContext
import com.moyi.liu.audiofeedback.domain.model.Boundary
import com.moyi.liu.audiofeedback.sensor.SensorBoundary
import kotlin.math.*

class SensorDataTransformer(
    private val frontBackAxisInitialValue: Float,
    frontBackBoundaries: Pair<Boundary, Boundary>, //<Front, Back>
    inline val boundaryTransformer: BoundaryTransformer
) {

    private val frontBoundary = boundaryTransformer(frontBackBoundaries.first)
    private val backBoundary = boundaryTransformer(frontBackBoundaries.second)

    /**
     * @param axisSensorValue front-back axis sensor value, refer to [https://developer.android.com/reference/android/hardware/SensorEvent]
     * @return a pair of Font and Back [AudioContext]
     */
    fun transformForFrontBackTracks(axisSensorValue: Float): Pair<AudioContext, AudioContext> {
        // axisSensorValue > frontBackAxisInitialValue ==> back
        // axisSensorValue <= frontBackAxisInitialValue ==> front
        return when {
            axisSensorValue > frontBackAxisInitialValue ->
                AudioContext.MUTE to axisSensorValue.transformToPlayRateAudioContext(backBoundary)
            else ->
                axisSensorValue.transformToLoudnessAudioContext(frontBoundary) to AudioContext.MUTE
        }
    }
}

typealias BoundaryTransformer = (Boundary) -> SensorBoundary

val GravitySensorBoundaryTransformer: BoundaryTransformer = { (front, back) ->
    SensorBoundary(
        front.angleToGravitySensorValue(),
        back.angleToGravitySensorValue()
    )
}


fun Float.transformToLoudnessAudioContext(boundary: SensorBoundary): AudioContext {
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

fun Float.transformToPlayRateAudioContext(boundary: SensorBoundary): AudioContext {
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