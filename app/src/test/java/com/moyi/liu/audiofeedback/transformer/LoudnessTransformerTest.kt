package com.moyi.liu.audiofeedback.transformer

import com.google.common.truth.Truth.assertThat
import com.moyi.liu.audiofeedback.domain.model.Boundary
import org.junit.Test
import kotlin.math.pow

class LoudnessTransformerTest {

    @Test
    fun givenSensorDataValueIsSmallerThanMin_volumeShouldBeMin() {
        val boundary = Boundary(1f, 3f)
        val value = 0.5f

        val result = value.transformToLoudnessAudioContext(boundary)

        assertThat(result.volume).isEqualTo(MIN_VOLUME)
        assertThat(result.playRate).isEqualTo(NORMAL_PLAY_RATE)
    }

    @Test
    fun givenSensorDataValueIsLargerThanMax_volumeShouldBeMax() {
        val boundary = Boundary(1f, 3f)
        val value = 3.1f

        val result = value.transformToLoudnessAudioContext(boundary)

        assertThat(result.volume).isEqualTo(MAX_VOLUME)
        assertThat(result.playRate).isEqualTo(NORMAL_PLAY_RATE)
    }

    @Test
    fun givenSensorDataValueIsEqualToMax_volumeShouldBeMax() {
        val boundary = Boundary(1f, 3f)
        val value = 3f

        val result = value.transformToLoudnessAudioContext(boundary)

        assertThat(result.volume).isEqualTo(MAX_VOLUME)
        assertThat(result.playRate).isEqualTo(NORMAL_PLAY_RATE)
    }

    @Test
    fun givenSensorDataValueInRange_volumeShouldBeSquaredProportional() {
        val boundary = Boundary(1f, 3f)
        val value = 2f

        val (min, max) = boundary

        val expected = ((value - min) / (max - min)).pow(2)

        val result = value.transformToLoudnessAudioContext(boundary)

        assertThat(result.volume).isEqualTo(expected)
        assertThat(result.playRate).isEqualTo(NORMAL_PLAY_RATE)
    }
}