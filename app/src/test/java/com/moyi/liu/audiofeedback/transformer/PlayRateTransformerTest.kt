package com.moyi.liu.audiofeedback.transformer

import com.google.common.truth.Truth
import com.moyi.liu.audiofeedback.sway.Boundary
import org.junit.Test
import kotlin.math.pow

class PlayRateTransformerTest {
    @Test
    fun givenSensorDataValueIsSmallerThanMin_volumeShouldBeMin() {
        val boundary = Boundary(1f, 3f)
        val value = 0.5f

        val result = value.transformToPlayRateAudioContext(boundary)

        Truth.assertThat(result.volume).isEqualTo(MIN_VOLUME)
    }

    @Test
    fun givenSensorDataValueIsEqualToMax_playRateShouldBeMax_volumeShouldBeMax() {
        val boundary = Boundary(1f, 3f)
        val value = 3f

        val result = value.transformToPlayRateAudioContext(boundary)

        Truth.assertThat(result.volume).isEqualTo(MAX_VOLUME)
        Truth.assertThat(result.playRate).isEqualTo(MAX_PLAY_RATE)
    }

    @Test
    fun givenSensorDataValueIsLargerThanMax_playRateShouldBeMax_volumeShouldBeMax() {
        val boundary = Boundary(1f, 3f)
        val value = 3.1f

        val result = value.transformToPlayRateAudioContext(boundary)

        Truth.assertThat(result.volume).isEqualTo(MAX_VOLUME)
        Truth.assertThat(result.playRate).isEqualTo(MAX_PLAY_RATE)
    }

    @Test
    fun givenSensorDataValueIsInRange_playRateShouldBeSquaredProportional_volumeShouldBeMax() {
        val boundary = Boundary(3f, 6f)
        val value = 4f
        val (min, max) = boundary

        val expected = NORMAL_PLAY_RATE + ((value - min) / (max - min)).pow(2)

        val result = value.transformToPlayRateAudioContext(boundary)

        Truth.assertThat(result.volume).isEqualTo(MAX_VOLUME)
        Truth.assertThat(result.playRate).isEqualTo(expected)
    }
}