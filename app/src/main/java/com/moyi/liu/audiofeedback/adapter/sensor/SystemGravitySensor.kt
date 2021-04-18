package com.moyi.liu.audiofeedback.adapter.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.moyi.liu.audiofeedback.domain.sensor.SensorInitialisationFailedException
import com.moyi.liu.audiofeedback.domain.sensor.SensorNotFoundException
import kotlin.jvm.Throws

interface SystemGravitySensor {
    @Throws(SensorInitialisationFailedException::class, SensorNotFoundException::class)
    fun initialise()

    @Throws(SensorInitialisationFailedException::class, SensorNotFoundException::class)
    fun register(sensorValuesUpdateListener: SensorValuesUpdateListener)

    fun unregister()
}


internal class AFSystemGravitySensor(private val ctx: Context) : SystemGravitySensor {
    private var sensorManager: SensorManager? = null
    private var _sensor: Sensor? = null

    private var sensorListener: SensorEventListener? = null

    @Throws(SensorInitialisationFailedException::class, SensorNotFoundException::class)
    override fun initialise() {
        initSensor()
        checkSensor()
    }

    @Throws(SensorInitialisationFailedException::class, SensorNotFoundException::class)
    override fun register(sensorValuesUpdateListener: SensorValuesUpdateListener) {
        checkSensor()

        if (sensorListener != null) unregister()

        sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.values?.also { v ->
                    sensorValuesUpdateListener(v[0], v[1], v[2])
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

        }

        sensorManager!!.registerListener(
            sensorListener,
            _sensor,
            SensorManager.SENSOR_DELAY_FASTEST
        )
    }

    override fun unregister() {
        if (sensorListener != null) {
            sensorManager?.unregisterListener(sensorListener, _sensor)
            sensorListener = null
        }
    }

    private fun initSensor() {
        sensorManager = (ctx.getSystemService(Context.SENSOR_SERVICE) as? SensorManager)
            ?.apply {
                _sensor = getSensorList(Sensor.TYPE_GRAVITY)
                    .firstOrNull { it.type == Sensor.TYPE_GRAVITY }
            }
    }

    private fun checkSensor() {
        if (sensorManager == null) throw SensorInitialisationFailedException
        if (_sensor == null) throw SensorNotFoundException
    }
}

internal typealias SensorValuesUpdateListener = (Float, Float, Float) -> Unit