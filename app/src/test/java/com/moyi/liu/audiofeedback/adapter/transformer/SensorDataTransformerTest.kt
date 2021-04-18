package com.moyi.liu.audiofeedback.adapter.transformer

import com.google.common.truth.Truth.assertThat
import com.moyi.liu.audiofeedback.domain.model.AudioContext
import com.moyi.liu.audiofeedback.domain.model.Boundary
import com.moyi.liu.audiofeedback.domain.model.Direction
import com.moyi.liu.audiofeedback.domain.model.PowerAccumulatorConfig
import org.junit.Test
import kotlin.math.abs

class SensorDataTransformerTest {
    private val boundaries = Boundary(2f, 30f) to Boundary(2f, 30f)
    private val origin = 1f
    private val accumulatorConfig = PowerAccumulatorConfig(20)
    private val transformer = SensorDataTransformer(
        frontBackAxisOriginValue = origin,
        frontBackBoundaries = boundaries,
        leftRightAxisOriginValue = origin,
        leftRightBoundaries = boundaries,
        accumulatorConfig = accumulatorConfig
    )

    @Test
    fun maxPowerShould() {
        val result =
            ((accumulatorConfig.powerCap + 1) / transformer.maxPower * accumulatorConfig.intakeIntervalMillis).toInt()
        assertThat(result).isEqualTo(MIN_SINGLE_NOTE_PLAY_INTERVAL_MILLIS)
    }

    @Test
    fun swayBackInRange() {
        val (_, boundary) = boundaries
        val sensorValue = -3.5f
        val sensorDiffValue = abs(sensorValue - origin)
        val expected =
            sensorDiffValue.gravitySensorValueToAngle().transformToPlayRateAudioContext(boundary)

        val (frontResult, backResult) = transformer.transformForFrontBackTracks(sensorValue)

        assertThat(backResult).isEqualTo(expected)
        assertThat(frontResult).isEqualTo(AudioContext.MUTE)

        assertThat(backResult).isNotEqualTo(AudioContext.MUTE)
    }

    @Test
    fun swayFrontInRange() {
        val (boundary, _) = boundaries

        val sensorValue = 4.5f
        val sensorDiffValue = sensorValue - origin
        val expected =
            sensorDiffValue.gravitySensorValueToAngle().transformToLoudnessAudioContext(boundary)

        val (frontResult, backResult) = transformer.transformForFrontBackTracks(sensorValue)

        assertThat(frontResult).isEqualTo(expected)
        assertThat(backResult).isEqualTo(AudioContext.MUTE)

        assertThat(frontResult).isNotEqualTo(AudioContext.MUTE)
    }

    @Test
    fun swayLeftInRange() {
        val (boundary, _) = boundaries

        val sensorValue = 4.5f
        val sensorDiffValue = sensorValue - origin
        val expectedPower =
            sensorDiffValue.gravitySensorValueToAngle()
                .transformToPowerValue(boundary, transformer.maxPower)

        val (direction, power) = transformer.transformForLeftRightTracks(sensorValue)

        assertThat(direction).isEqualTo(Direction.LEFT)
        assertThat(power).isEqualTo(expectedPower)
    }

    @Test
    fun swayRightInRange() {
        val (_, boundary) = boundaries

        val sensorValue = -4.5f
        val sensorDiffValue = sensorValue - origin
        val expectedPower =
            sensorDiffValue.gravitySensorValueToAngle()
                .transformToPowerValue(boundary, transformer.maxPower)

        val (direction, power) = transformer.transformForLeftRightTracks(sensorValue)

        assertThat(direction).isEqualTo(Direction.RIGHT)
        assertThat(power).isEqualTo(expectedPower)
    }
}