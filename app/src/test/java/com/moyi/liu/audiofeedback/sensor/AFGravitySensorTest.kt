package com.moyi.liu.audiofeedback.sensor

import com.google.common.truth.Truth.assertThat
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

class AFGravitySensorTest {

    companion object {
        @BeforeClass
        @JvmStatic
        fun setup() {
            RxAndroidPlugins.setInitMainThreadSchedulerHandler {
                Schedulers.trampoline()
            }
        }

        @AfterClass
        @JvmStatic
        fun teardown() {
            RxAndroidPlugins.reset()
        }
    }

    @Test
    fun givenSensorIsNotFound_whenCallingInitialisation_shouldThrowSensorNotFoundException() {
        val sysSensor = object : SystemGravitySensor {
            override fun initialise() = throw SensorNotFoundException

            override fun register(sensorValuesUpdateListener: SensorValuesUpdateListener) {}
            override fun unregister() {}
        }

        AFGravitySensor(sysSensor)
            .initialiseSensor()
            .test()
            .assertError(SensorNotFoundException)
            .assertNotComplete()
    }

    @Test
    fun givenSensorIsNotFound_whenCallingRegister_shouldThrowSensorNotFoundException() {
        val sysSensor = object : SystemGravitySensor {
            override fun register(sensorValuesUpdateListener: SensorValuesUpdateListener) =
                throw SensorNotFoundException

            override fun initialise() {}
            override fun unregister() {}
        }

        AFGravitySensor(sysSensor)
            .register()
            .test()
            .assertError(SensorNotFoundException)
            .assertNotComplete()

    }

    @Test
    fun givenSensorIsRegistered_shouldReceiveSensorDataStream() {
        var listener: SensorValuesUpdateListener? = null
        val sysSensor = object : SystemGravitySensor {

            override fun register(sensorValuesUpdateListener: SensorValuesUpdateListener) {
                listener = sensorValuesUpdateListener
            }

            override fun initialise() {}
            override fun unregister() {}
        }

        val sensor = AFGravitySensor(sysSensor)

        sensor.register()
            .test()

        assertThat(listener).isNotNull()

        val dataStreamObserver = sensor.sensorDataStream.test()

        listener?.run {
            invoke(1f, 1f, 1f)
            invoke(2f, 2f, 2f)
            invoke(3f, 3f, 3f)
        }

        val expectedResultSequence = mutableListOf(
            Triple(1f, 1f, 1f),
            Triple(2f, 2f, 2f),
            Triple(3f, 3f, 3f)
        )

        dataStreamObserver.assertValueSequence(expectedResultSequence)
    }
}