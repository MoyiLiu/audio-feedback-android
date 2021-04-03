package com.moyi.liu.audiofeedback.transformer

import com.google.common.truth.Truth.assertThat
import com.moyi.liu.audiofeedback.domain.model.Boundary
import com.moyi.liu.audiofeedback.domain.model.PowerAccumulatorConfig
import org.junit.Test

class PowerValueTransformer {
    private val boundaries = Boundary(10f, 25f)
    private val accumulatorConfig = PowerAccumulatorConfig(100f, 20)

    @Test
    fun givenSwayAngleSmallerThanMin_powerIsZero() {
        val angle = 5f
        val result = angle.transformToPowerValue(boundaries, accumulatorConfig)

        assertThat(result).isEqualTo(0f)
    }

    @Test
    fun givenSwayAngleIsLargerThanMax_powerIsMax() {
        val angle = 26f
        val result = angle.transformToPowerValue(boundaries, accumulatorConfig)

        assertThat(result).isEqualTo(accumulatorConfig.powerCap / MAX_SINGLE_NOTE_PER_SECOND)
    }

    //TODO add tests when sway-angle-power trajectory is finalised
}