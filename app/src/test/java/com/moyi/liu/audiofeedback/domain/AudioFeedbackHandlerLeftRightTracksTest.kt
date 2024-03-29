package com.moyi.liu.audiofeedback.domain

import com.google.common.truth.Truth.assertThat
import com.moyi.liu.audiofeedback.adapter.audio.StubAudioManager
import com.moyi.liu.audiofeedback.adapter.transformer.MAX_GRAVITY_SENSOR_VALUE
import com.moyi.liu.audiofeedback.adapter.transformer.MIN_SINGLE_NOTE_PLAY_INTERVAL_MILLIS
import com.moyi.liu.audiofeedback.adapter.transformer.SensorDataTransformer
import com.moyi.liu.audiofeedback.domain.calibration.SensorCalibrator
import com.moyi.liu.audiofeedback.domain.model.CalibrationConfig
import com.moyi.liu.audiofeedback.domain.model.PowerAccumulatorConfig
import com.moyi.liu.audiofeedback.domain.model.STUB_BOUNDARY
import com.moyi.liu.audiofeedback.domain.power.AFPowerAccumulator
import com.moyi.liu.audiofeedback.domain.power.AFPowerStore
import com.moyi.liu.audiofeedback.domain.sensor.StubGravitySensor
import com.moyi.liu.audiofeedback.domain.usecase.CalibrationUseCase
import com.moyi.liu.audiofeedback.stub.StubMessageStore
import com.moyi.liu.audiofeedback.stub.StubVoiceoverController
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

class AudioFeedbackHandlerLeftRightTracksTest {
    companion object {
        @BeforeClass
        @JvmStatic
        fun setup() {
            RxAndroidPlugins.setInitMainThreadSchedulerHandler {
                Schedulers.trampoline()
            }
            RxJavaPlugins.setIoSchedulerHandler {
                Schedulers.trampoline()
            }
        }

        @AfterClass
        @JvmStatic
        fun teardown() {
            RxAndroidPlugins.reset()
            RxJavaPlugins.reset()
        }
    }

    private val boundaries = STUB_BOUNDARY to STUB_BOUNDARY
    private val accumulatorConfig = PowerAccumulatorConfig(20)
    private val transformer = SensorDataTransformer(
        frontBackAxisOriginValue = 0f,
        frontBackBoundaries = boundaries,
        leftRightAxisOriginValue = 0f,
        leftRightBoundaries = boundaries,
        accumulatorConfig = accumulatorConfig
    )
    private val powerStore =
        AFPowerStore(AFPowerAccumulator(accumulatorConfig), AFPowerAccumulator(accumulatorConfig))
    private val gravitySensor = StubGravitySensor()
    private val audioManager = StubAudioManager()

    private val calibrationUseCase = CalibrationUseCase(
        messageStore = StubMessageStore(),
        calibrator = SensorCalibrator(StubGravitySensor()),
        voiceoverController = StubVoiceoverController(),
        calibrationConfig = CalibrationConfig(2, 3)
    )

    @Test
    fun shouldReceiveLeftChargedSignal() {
        val handler = AudioFeedbackHandler(
            sensor = gravitySensor,
            audioManager = audioManager,
            calibrationUseCase = calibrationUseCase
        ).also {
            it.powerStore = powerStore
            it.dataTransformer = transformer
        }

        handler.setup().test()
        handler.start().test()

        val minNumberOfDataToTriggerOneNote =
            MIN_SINGLE_NOTE_PLAY_INTERVAL_MILLIS / accumulatorConfig.intakeIntervalMillis
        for (i in 0 until minNumberOfDataToTriggerOneNote) {
            gravitySensor.sensorDataStream.onNext(Triple(9.81f, 0f, 0f))
        }

        assertThat(audioManager.directions).hasSize(1)

    }

    @Test
    fun givenDirectionIsChanged_accumulatorsShouldBeEmptied() {
        val handler = AudioFeedbackHandler(
            sensor = gravitySensor,
            audioManager = audioManager,
            calibrationUseCase = calibrationUseCase
        ).also {
            it.powerStore = powerStore
            it.dataTransformer = transformer
        }

        handler.setup().test()
        handler.start().test()

        val minNumberOfDataToTriggerOneNote =
            MIN_SINGLE_NOTE_PLAY_INTERVAL_MILLIS / accumulatorConfig.intakeIntervalMillis
        for (i in 0 until minNumberOfDataToTriggerOneNote - 1) {
            gravitySensor.sensorDataStream.onNext(Triple(MAX_GRAVITY_SENSOR_VALUE, 0f, 0f))
        }

        for (i in 0 until minNumberOfDataToTriggerOneNote - 1) {
            gravitySensor.sensorDataStream.onNext(Triple(-MAX_GRAVITY_SENSOR_VALUE, 0f, 0f))
        }

        gravitySensor.sensorDataStream.onNext(Triple(MAX_GRAVITY_SENSOR_VALUE, 0f, 0f))


        assertThat(audioManager.directions).hasSize(0)

    }
}