package com.moyi.liu.audiofeedback.domain

import com.google.common.truth.Truth.assertThat
import com.moyi.liu.audiofeedback.adapter.audio.StubAudioManager
import com.moyi.liu.audiofeedback.domain.model.PowerAccumulatorConfig
import com.moyi.liu.audiofeedback.domain.model.STUB_BOUNDARY
import com.moyi.liu.audiofeedback.domain.power.StubPowerStore
import com.moyi.liu.audiofeedback.domain.power.getStubChargedIndicators
import com.moyi.liu.audiofeedback.rx.StubDisposable
import com.moyi.liu.audiofeedback.adapter.sensor.SensorInitialisationFailedException
import com.moyi.liu.audiofeedback.adapter.sensor.SensorNotFoundException
import com.moyi.liu.audiofeedback.adapter.sensor.StubGravitySensor
import com.moyi.liu.audiofeedback.adapter.transformer.SensorDataTransformer
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

class AudioFeedbackHandlerFrontBackTracksTest {

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
    private val powerStore = StubPowerStore(getStubChargedIndicators(2))
    private val audioManager = StubAudioManager()

    @Test
    fun givenSensorInitialisationFailed_handlerShouldThrowException() {
        val sensor = object : StubGravitySensor() {
            override fun initialiseSensor(): Completable = Completable.error(
                SensorInitialisationFailedException
            )
        }

        AudioFeedbackHandler(sensor, audioManager, transformer, powerStore)
            .setup()
            .test()
            .assertError(SensorInitialisationFailedException)

    }

    @Test
    fun givenDataStreamIsActive_whenCallingStart_shouldDisposeExistingOne() {
        val handler =
            AudioFeedbackHandler(StubGravitySensor(), audioManager, transformer, powerStore)
        val disposable = StubDisposable()
        handler.dataStreamDisposable = disposable

        handler.start().test()

        assertThat(disposable.isDisposeCalled).isTrue()
    }

    @Test
    fun givenStartIsCalled_andSensorRegisterFailed_shouldThrowError_andNoDataShouldBePassedToStream() {
        val sensor = object : StubGravitySensor() {
            override fun register(): Completable = Completable.error(SensorNotFoundException)
        }
        val handler = AudioFeedbackHandler(sensor, audioManager, transformer, powerStore)

        handler.start()
            .test()
            .assertError(SensorNotFoundException)

        sensor.sensorDataStream.onNext(Triple(1f, 1f, 1f))


        assertThat(audioManager.audioContextsList).isEmpty()
        assertThat(audioManager.isReleaseAllTracksCalled).isTrue()
        assertThat(powerStore.isShutdownCalled).isTrue()

    }

    @Test
    fun givenStartIsCalled_DataShouldBePassedToStream() {
        val sensor = StubGravitySensor()

        AudioFeedbackHandler(sensor, audioManager, transformer, powerStore)
            .start()
            .test()

        with(sensor.sensorDataStream) {
            onNext(Triple(1f, 1f, 1f))
            onNext(Triple(1f, 1f, 1f))
            onNext(Triple(1f, 1f, 1f))
        }

        assertThat(audioManager.audioContextsList).isNotEmpty()
        assertThat(audioManager.audioContextsList).hasSize(3)
    }

}