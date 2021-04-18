package com.moyi.liu.audiofeedback.adapter.transformer

import com.google.common.truth.Truth
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.math.sqrt

@RunWith(Parameterized::class)
class AngleToGravitySensorValueTest(
    private val angle: Float,
    private val expectedSensorValue: Float
) {
    @Test
    fun shouldConvertAngleToExpectedSensorValue() {
        Truth.assertThat(angle.angleToGravitySensorValue()).isEqualTo(expectedSensorValue)
    }

    private companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "angle {0}, expected sensor value {1}")
        fun data() = listOf(
            arrayOf(90f, MAX_GRAVITY_SENSOR_VALUE),
            arrayOf(0f, 0f),
            arrayOf(30, MAX_GRAVITY_SENSOR_VALUE / 2),
            arrayOf(45f, MAX_GRAVITY_SENSOR_VALUE * sqrt(2f) / 2)
        )
    }
}