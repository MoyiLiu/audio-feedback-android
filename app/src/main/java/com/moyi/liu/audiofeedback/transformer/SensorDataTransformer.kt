package com.moyi.liu.audiofeedback.transformer

import com.moyi.liu.audiofeedback.audio.AudioContext
import com.moyi.liu.audiofeedback.domain.model.Boundary
import com.moyi.liu.audiofeedback.domain.model.Direction
import com.moyi.liu.audiofeedback.domain.model.PowerAccumulatorConfig
import kotlin.math.*

class SensorDataTransformer(
    private val frontBackAxisOriginValue: Float,
    frontBackBoundaries: Pair<Boundary, Boundary>, //<Front, Back>
    private val leftRightAxisOriginValue: Float,
    leftRightBoundaries: Pair<Boundary, Boundary>, //<Front, Back>
    private val accumulatorConfig: PowerAccumulatorConfig
) {

    private val frontBoundary: Boundary = frontBackBoundaries.first
    private val backBoundary: Boundary = frontBackBoundaries.second
    private val leftBoundary: Boundary = leftRightBoundaries.first
    private val rightBoundary: Boundary = leftRightBoundaries.second

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

    fun transformForLeftRightTracks(axisSensorValue: Float): Pair<Direction, Float> {
        val direction = when {
            axisSensorValue > leftRightAxisOriginValue -> Direction.LEFT
            else -> Direction.RIGHT
        }

        val angle = abs(axisSensorValue - leftRightAxisOriginValue).gravitySensorValueToAngle()
        when (direction) {
            Direction.LEFT -> angle.transformToPowerValue(leftBoundary, accumulatorConfig)
            Direction.RIGHT -> angle.transformToPowerValue(rightBoundary, accumulatorConfig)
        }.let { powerValue ->
            return direction to powerValue
        }
    }
}

/**
 * Transform sway angle to power value
 * Using linear trajectory [Play Speed = Sway Angle] (y = x) for now
 * TODO Investigate other trajectories
 * Other possibilities:
 * - y = x^2
 * - y = exp(2 * x) * 1.37
 */
fun Float.transformToPowerValue(
    boundary: Boundary,
    accumulatorConfig: PowerAccumulatorConfig
): Float {
    val (min, max) = boundary
    val maxPower = accumulatorConfig.powerCap / MAX_SINGLE_NOTE_PER_SECOND

    return when {
        this < min -> 0f
        this >= max -> maxPower
        else -> {
//            val transformedSingleSensorRead = (this - min) / (max - min)
            maxPower * (this - min) / (max - min)
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