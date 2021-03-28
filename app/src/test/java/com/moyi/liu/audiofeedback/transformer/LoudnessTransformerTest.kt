package com.moyi.liu.audiofeedback.transformer

import com.google.common.truth.Truth.assertThat
import com.moyi.liu.audiofeedback.sway.Boundary
import org.junit.Test
import kotlin.math.pow

class LoudnessTransformerTest {

    @Test
    fun givenSensorDataValueIsLowerThanMin_volumeShouldBeZero() {
        val boundary = Boundary(1.0f, 3.0f)
        val value = 0.5f

        val result = value.transformToLoudnessAudioContext(boundary)

        assertThat(result.volume).isEqualTo(0f)
        assertThat(result.playRate).isEqualTo(1f)
    }

    @Test
    fun givenSensorDataValueIsLargerThanMax_volumeShouldBeOne() {
        val boundary = Boundary(1.0f, 3.0f)
        val value = 3.1f

        val result = value.transformToLoudnessAudioContext(boundary)

        assertThat(result.volume).isEqualTo(1f)
        assertThat(result.playRate).isEqualTo(1f)
    }

    @Test
    fun givenSensorDataValueIsEqualToMax_volumeShouldBeOne() {
        val boundary = Boundary(1.0f, 3.0f)
        val value = 3.0f

        val result = value.transformToLoudnessAudioContext(boundary)

        assertThat(result.volume).isEqualTo(1f)
        assertThat(result.playRate).isEqualTo(1f)
    }

    @Test
    fun givenSensorDataValueInRange_volumeShouldBeSquaredProportional() {
        val boundary = Boundary(1.0f, 3.0f)
        val value = 2.0f

        val expected = ((2.0f - 1.0f) / (3.0f - 1.0f)).pow(2)

        val result = value.transformToLoudnessAudioContext(boundary)

        assertThat(result.volume).isEqualTo(expected)
        assertThat(result.playRate).isEqualTo(1f)
    }
}