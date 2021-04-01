package com.moyi.liu.audiofeedback.transformer

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.text.DecimalFormat
import kotlin.math.sqrt

@RunWith(Parameterized::class)
class GravitySensorValueToAngleTest(
    private val sensorValue: Float,
    private val expectedAngle: Float
) {

    @Test
    fun shouldSensorValueToExpectedAngle() {
        assertThat(
            sensorValue.gravitySensorValueToAngle().roundToTwoDecimals()
        ).isEqualTo(expectedAngle)
    }

    private companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "sensor value {0}, expected angle {1}")
        fun data() = listOf(
            arrayOf(MAX_GRAVITY_SENSOR_VALUE, 90f),
            arrayOf(0f, 0f),
            arrayOf(200f, 90f),
            arrayOf(MAX_GRAVITY_SENSOR_VALUE / 2, 30),
            arrayOf(MAX_GRAVITY_SENSOR_VALUE * sqrt(2f) / 2, 45f)
        )

    }
}