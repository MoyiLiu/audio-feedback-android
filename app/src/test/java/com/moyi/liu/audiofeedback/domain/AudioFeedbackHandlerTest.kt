package com.moyi.liu.audiofeedback.domain

import com.google.common.truth.Truth.assertThat
import com.moyi.liu.audiofeedback.audio.StubAudioManager
import com.moyi.liu.audiofeedback.domain.model.STUB_BOUNDARY
import com.moyi.liu.audiofeedback.rx.StubDisposable
import com.moyi.liu.audiofeedback.sensor.SensorInitialisationFailedException
import com.moyi.liu.audiofeedback.sensor.SensorNotFoundException
import com.moyi.liu.audiofeedback.sensor.StubGravitySensor
import com.moyi.liu.audiofeedback.transformer.SensorDataTransformer
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

class AudioFeedbackHandlerTest {

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

    @Test
    fun givenSensorInitialisationFailed_handlerShouldThrowException() {
        val sensor = object : StubGravitySensor() {
            override fun initialiseSensor(): Completable = Completable.error(
                SensorInitialisationFailedException
            )
        }

        val transformer = SensorDataTransformer(
            frontBackAxisOriginValue = 0f,
            frontBackBoundaries = STUB_BOUNDARY to STUB_BOUNDARY
        )

        AudioFeedbackHandler(sensor, StubAudioManager(), transformer)
            .setup()
            .test()
            .assertError(SensorInitialisationFailedException)
    }

    @Test
    fun givenDataStreamIsActive_whenCallingStart_shouldDisposeExistingOne() {
        val transformer = SensorDataTransformer(
            frontBackAxisOriginValue = 0f,
            frontBackBoundaries = STUB_BOUNDARY to STUB_BOUNDARY
        )

        val handler = AudioFeedbackHandler(StubGravitySensor(), StubAudioManager(), transformer)
        val disposable = StubDisposable()
        handler.dataStreamDisposable = disposable

        handler.start()

        assertThat(disposable.isDisposeCalled).isTrue()
    }

    @Test
    fun givenStartIsCalled_andSensorRegisterFailed_shouldThrowError_andNoDataShouldBePassedToStream() {
        val transformer = SensorDataTransformer(
            frontBackAxisOriginValue = 0f,
            frontBackBoundaries = STUB_BOUNDARY to STUB_BOUNDARY
        )

        val sensor = object : StubGravitySensor() {
            override fun register(): Completable = Completable.error(SensorNotFoundException)
        }
        val audioManager = StubAudioManager()
        val handler = AudioFeedbackHandler(sensor, audioManager, transformer)

        handler.start()
            .test()
            .assertError(SensorNotFoundException)

        sensor.sensorDataStream.onNext(Triple(1f, 1f, 1f))


        assertThat(audioManager.audioContextsList).isEmpty()
    }

    @Test
    fun givenStartIsCalled_DataShouldBePassedToStream() {
        val transformer = SensorDataTransformer(
            frontBackAxisOriginValue = 0f,
            frontBackBoundaries = STUB_BOUNDARY to STUB_BOUNDARY
        )

        val sensor = StubGravitySensor()

        val audioManager = StubAudioManager()
        AudioFeedbackHandler(sensor, audioManager, transformer)
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