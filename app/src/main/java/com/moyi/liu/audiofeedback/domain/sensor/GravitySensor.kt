package com.moyi.liu.audiofeedback.domain.sensor

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.subjects.PublishSubject

interface GravitySensor {
    val sensorDataStream: PublishSubject<Triple<Float, Float, Float>>

    fun initialiseSensor(): Completable
    fun register(): Completable
    fun unregister()
}

object SensorInitialisationFailedException: Exception()
object SensorNotFoundException: Exception()