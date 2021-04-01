package com.moyi.liu.audiofeedback.transformer

import com.moyi.liu.audiofeedback.audio.AudioContext
import com.moyi.liu.audiofeedback.domain.model.Boundary
import com.moyi.liu.audiofeedback.sensor.SensorBoundary
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.pow
import kotlin.math.sin

class SensorDataTransformer(
    private val frontBackAxisInitialValue: Float,
    frontBackBoundaries: Pair<Boundary, Boundary>, //<Front, Back>
    boundaryTransformer: BoundaryTransformer
) {

    private val frontBoundary = boundaryTransformer(frontBackBoundaries.first)
    private val backBoundary = boundaryTransformer(frontBackBoundaries.second)

    /**
     * @param sensorData x,y,z axes values, refer to [https://developer.android.com/reference/android/hardware/SensorEvent]
     * @return a pair of Font and Back [AudioContext]
     */
    fun transformForFrontBackTracks(sensorData: Triple<Float, Float, Float>): Pair<AudioContext, AudioContext> {
        val (_, _, value) = sensorData
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
        asin(sensorValue / MAX_GRAVITY_SENSOR_VALUE)
    }

fun Float.angleToGravitySensorValue(): Float =
    sin(this) * MAX_GRAVITY_SENSOR_VALUE