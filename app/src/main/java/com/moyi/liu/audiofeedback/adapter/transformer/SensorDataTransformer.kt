package com.moyi.liu.audiofeedback.adapter.transformer

import com.moyi.liu.audiofeedback.domain.model.AudioContext
import com.moyi.liu.audiofeedback.domain.model.Boundary
import com.moyi.liu.audiofeedback.domain.model.Direction
import com.moyi.liu.audiofeedback.domain.model.PowerAccumulatorConfig
import kotlin.math.*

class SensorDataTransformer(
    private val frontBackAxisOriginValue: Float,
    frontBackBoundaries: Pair<Boundary, Boundary>, //<Front, Back>
    private val leftRightAxisOriginValue: Float,
    leftRightBoundaries: Pair<Boundary, Boundary>, //<Front, Back>
    accumulatorConfig: PowerAccumulatorConfig
) {

    private val frontBoundary: Boundary = frontBackBoundaries.first
    private val backBoundary: Boundary = frontBackBoundaries.second
    private val leftBoundary: Boundary = leftRightBoundaries.first
    private val rightBoundary: Boundary = leftRightBoundaries.second

    //[accumulatorConfig.powerCap + 1] ==>> tiny calibration to avoid division round down to insufficient power, e.g. 10/3 = 3.33, 3.33 * 3 = 9.99 < 10
    //[MIN_SINGLE_NOTE_PLAY_INTERVAL_MILLIS / accumulatorConfig.intakeIntervalMillis] ==>> number of power data points needed to cover the shortest period of two single note plays
    val maxPower =
        (accumulatorConfig.powerCap + 1) / (MIN_SINGLE_NOTE_PLAY_INTERVAL_MILLIS / accumulatorConfig.intakeIntervalMillis)

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
            Direction.LEFT -> angle.transformToPowerValue(leftBoundary, maxPower)
            Direction.RIGHT -> angle.transformToPowerValue(rightBoundary, maxPower)
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
    maxPower: Float
): Float {
    val (min, max) = boundary
    return when {
        this < min -> 0f
        this >= max -> maxPower
        else -> {
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