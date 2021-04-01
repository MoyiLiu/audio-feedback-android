package com.moyi.liu.audiofeedback.transformer

import com.google.common.truth.Truth.assertThat
import com.moyi.liu.audiofeedback.audio.AudioContext
import com.moyi.liu.audiofeedback.sway.Boundary
import org.junit.Test

class SensorDataTransformerTest {
    @Test
    fun swayBackInRange() {
        val boundaries = Boundary(-6f, -2f) to Boundary(2f, 6f)
        val transformer = SensorDataTransformer(
            frontBackAxisInitialValue = 1f,
            frontBackBoundaries = boundaries
        )

        val (_, boundary) = boundaries
        val value = 5.5f
        val expected = value.transformToPlayRateAudioContext(boundary)
        val sensorData = Triple(0f, 0f, value)

        val (frontResult, backResult) = transformer.transformForFrontBackTracks(sensorData)

        assertThat(backResult).isEqualTo(expected)
        assertThat(frontResult).isEqualTo(AudioContext.MUTE)

        assertThat(backResult).isNotEqualTo(AudioContext.MUTE)
    }

    @Test
    fun swayFrontInRange() {
        val boundaries = Boundary(-6f, -2f) to Boundary(2f, 6f)
        val transformer = SensorDataTransformer(
            frontBackAxisInitialValue = 1f,
            frontBackBoundaries = boundaries
        )

        val (boundary, _) = boundaries
        val value = -4.5f
        val expected = value.transformToLoudnessAudioContext(boundary)
        val sensorData = Triple(0f, 0f, value)

        val (frontResult, backResult) = transformer.transformForFrontBackTracks(sensorData)

        assertThat(frontResult).isEqualTo(expected)
        assertThat(backResult).isEqualTo(AudioContext.MUTE)

        assertThat(frontResult).isNotEqualTo(AudioContext.MUTE)
    }
}