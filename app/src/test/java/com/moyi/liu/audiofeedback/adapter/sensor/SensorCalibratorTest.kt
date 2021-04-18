package com.moyi.liu.audiofeedback.adapter.sensor

import com.google.common.truth.Truth.assertThat
import com.moyi.liu.audiofeedback.domain.calibration.SensorCalibrator
import com.moyi.liu.audiofeedback.domain.model.CalibrationResult
import com.moyi.liu.audiofeedback.domain.model.Origin
import com.moyi.liu.audiofeedback.domain.sensor.StubGravitySensor
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.TestScheduler
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import java.util.concurrent.TimeUnit

class SensorCalibratorTest {

    companion object {
        private val scheduler = TestScheduler()

        @BeforeClass
        @JvmStatic
        fun setup() {
            RxAndroidPlugins.setInitMainThreadSchedulerHandler {
                scheduler
            }
            RxJavaPlugins.setIoSchedulerHandler { scheduler }
        }

        @AfterClass
        @JvmStatic
        fun teardown() {
            RxAndroidPlugins.reset()
            RxJavaPlugins.reset()
        }
    }

    @Test
    fun sensorRegistered_receiveSensorDataStream_shouldReturnTheMeanValueOfDataStream() {
        val sensor = StubGravitySensor()

        val countDownRecord = mutableListOf<Long>()

        val sensorCalibrator = SensorCalibrator(sensor)
        val maybe = sensorCalibrator.startCalibration(3) { countDownRecord.add(it) }
        val observer = maybe.test()

        sensor.sensorDataStream.onNext(Triple(1f, 3f, 1f))
        sensor.sensorDataStream.onNext(Triple(2f, 6f, 2f))
        sensor.sensorDataStream.onNext(Triple(6f, 9f, 3f))

        scheduler.advanceTimeBy(4L, TimeUnit.SECONDS)

        assertThat(sensor.sensorDataStream.hasComplete()).isTrue()

        observer.assertComplete()
        observer.assertValue(
            CalibrationResult(
                origin = Origin(3f, 6f, 2f),
                numberOfDataPoints = 3
            )
        )
        assertThat(countDownRecord).containsExactly(1L, 2L, 3L)
    }
}