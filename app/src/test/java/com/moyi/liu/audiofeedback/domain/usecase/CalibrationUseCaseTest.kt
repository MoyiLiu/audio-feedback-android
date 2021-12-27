package com.moyi.liu.audiofeedback.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.moyi.liu.audiofeedback.domain.calibration.SensorCalibrator
import com.moyi.liu.audiofeedback.domain.model.CalibrationConfig
import com.moyi.liu.audiofeedback.domain.model.CalibrationResult
import com.moyi.liu.audiofeedback.domain.model.Origin
import com.moyi.liu.audiofeedback.domain.sensor.StubGravitySensor
import com.moyi.liu.audiofeedback.stub.StubMessageStore
import com.moyi.liu.audiofeedback.stub.StubVoiceoverController
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.TestScheduler
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import java.util.concurrent.TimeUnit

class CalibrationUseCaseTest {

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
    fun noErrorIsThrownFromOtherComponents_calibratorUseCaseReturnsCalibrationResult() {
        val sensor = StubGravitySensor()
        val voiceoverController = StubVoiceoverController()
        val calibrationConfig = CalibrationConfig(2, 3)

        val useCase = CalibrationUseCase(
            messageStore = StubMessageStore(),
            calibrator = SensorCalibrator(sensor),
            voiceoverController = voiceoverController,
            calibrationConfig = calibrationConfig
        )

        val o = useCase.startCalibration().test()

        assertThat(voiceoverController.isInitialiseCalled).isTrue()

        //Preparation countdown
        scheduler.advanceTimeBy(2, TimeUnit.SECONDS)
        //Calibration countdown
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS)
        //Emit sensor data during countdown
        sensor.sensorDataStream.onNext(Triple(2f, 2f, 2f))
        sensor.sensorDataStream.onNext(Triple(4f, 4f, 4f))
        //Continue Calibration countdown
        scheduler.advanceTimeBy(2, TimeUnit.SECONDS)


        assertThat(voiceoverController.speakMessages).containsExactly(
            //Speak out calibration preparation message with config value, e.g. give user time to attach device on lower back
            "preparation 2",
            //Preparation countdown,
            "2",
            "1",
            "0",
            //Speak out start calibration message with config value
            "start calibration 3",
            //Calibration countdown
            "3",
            "2",
            "1",
            "0"
        )

        o.assertValue(
            CalibrationResult(
                origin = Origin(3f, 3f, 3f),
                numberOfDataPoints = 2
            )
        )

        assertThat(voiceoverController.isDestroyCalled).isTrue()
    }
}