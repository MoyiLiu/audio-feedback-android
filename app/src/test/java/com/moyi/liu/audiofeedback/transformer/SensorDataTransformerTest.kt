package com.moyi.liu.audiofeedback.transformer

import com.google.common.truth.Truth.assertThat
import org.junit.Test

import com.moyi.liu.audiofeedback.audio.AudioContext
import com.moyi.liu.audiofeedback.sensor.SensorBoundary
import com.moyi.liu.audiofeedback.domain.model.Boundary

class SensorDataTransformerTest {
    @Test
    fun swayBackInRange() {
        val boundaries = Boundary(-6f, -2f) to Boundary(2f, 6f)
        val transformer = SensorDataTransformer(
            frontBackAxisInitialValue = 1f,
            frontBackBoundaries = boundaries,
            boundaryTransformer = StubBoundaryTransformer
        )

        val (_, boundary) = boundaries
        val value = 5.5f
        val expected = value.transformToPlayRateAudioContext(StubBoundaryTransformer(boundary))
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
            frontBackBoundaries = boundaries,
            boundaryTransformer = StubBoundaryTransformer
        )

        val (boundary, _) = boundaries
        val value = -4.5f
        val expected = value.transformToLoudnessAudioContext(StubBoundaryTransformer(boundary))
        val sensorData = Triple(0f, 0f, value)

        val (frontResult, backResult) = transformer.transformForFrontBackTracks(sensorData)

        assertThat(frontResult).isEqualTo(expected)
        assertThat(backResult).isEqualTo(AudioContext.MUTE)

        assertThat(frontResult).isNotEqualTo(AudioContext.MUTE)
    }
}