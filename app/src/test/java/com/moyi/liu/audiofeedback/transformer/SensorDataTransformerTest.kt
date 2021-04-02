package com.moyi.liu.audiofeedback.transformer

import com.google.common.truth.Truth.assertThat
import com.moyi.liu.audiofeedback.audio.AudioContext
import com.moyi.liu.audiofeedback.domain.model.Boundary
import org.junit.Test
import kotlin.math.abs

class SensorDataTransformerTest {
    @Test
    fun swayBackInRange() {
        val boundaries = Boundary(2f, 6f) to Boundary(2f, 6f)
        val origin = 1f
        val transformer = SensorDataTransformer(
            frontBackAxisOriginValue = origin,
            frontBackBoundaries = boundaries
        )

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
        val boundaries = Boundary(6f, 2f) to Boundary(2f, 6f)
        val origin = 1f
        val transformer = SensorDataTransformer(
            frontBackAxisOriginValue = origin,
            frontBackBoundaries = boundaries
        )

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
}