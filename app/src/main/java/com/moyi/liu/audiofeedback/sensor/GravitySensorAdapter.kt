package com.moyi.liu.audiofeedback.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.CompletableEmitter
import io.reactivex.rxjava3.subjects.PublishSubject

class GravitySensorAdapter(
    private val ctx: Context
) : GravitySensor, SensorEventListener {
    override val sensorDataStream: PublishSubject<Triple<Float, Float, Float>> =
        PublishSubject.create()


    private var sensorManager: SensorManager? = null
    private var sensor: Sensor? = null

    override fun initialiseSensor(): Completable = Completable.create { emitter ->
        if (sensorManager != null && sensor != null) emitter.onComplete()
        else {
            initSensor()
            checkSensorWith(emitter)
        }
        emitter.onComplete()
    }.subscribeOn(AndroidSchedulers.mainThread())

    override fun register(): Completable =
        Completable.create { emitter ->
            checkSensorWith(emitter)

            sensorManager!!.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST)
            emitter.onComplete()
        }.subscribeOn(AndroidSchedulers.mainThread())

    override fun unregister() {
        sensorManager?.unregisterListener(this, sensor)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.also { e ->
            sensorDataStream.onNext(Triple(e.values[0], e.values[1], e.values[2]))
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun initSensor() {
        sensorManager = (ctx.getSystemService(Context.SENSOR_SERVICE) as? SensorManager)
            ?.apply {
                sensor = getSensorList(Sensor.TYPE_GRAVITY)
                    .firstOrNull { it.type == Sensor.TYPE_GRAVITY }
            }
    }

    private fun checkSensorWith(emitter: CompletableEmitter) {
        if (sensorManager == null) emitter.onError(SensorInitialisationFailedException)
        if (sensor == null) emitter.onError(SensorNotFoundException)
    }
}