package com.moyi.liu.audiofeedback.adapter.transformer

import com.google.common.truth.Truth.assertThat
import com.moyi.liu.audiofeedback.domain.model.Boundary
import com.moyi.liu.audiofeedback.domain.model.PowerAccumulatorConfig
import org.junit.Test

class PowerValueTransformerTest {
    private val boundaries = Boundary(10f, 25f)
    private val accumulatorConfig = PowerAccumulatorConfig(20)

    @Test
    fun givenSwayAngleSmallerThanMin_powerIsZero() {
        val angle = 5f
        val result = angle.transformToPowerValue(boundaries, 34f)

        assertThat(result).isEqualTo(0f)
    }

    @Test
    fun givenSwayAngleIsLargerThanMax_powerIsMax() {
        val angle = 26f
        val result = angle.transformToPowerValue(boundaries, 34f)

        assertThat(result).isEqualTo(34f)
    }

    //TODO add tests when sway-angle-power trajectory is finalised
}