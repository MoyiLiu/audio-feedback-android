package com.moyi.liu.audiofeedback.adapter.sensor

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.subjects.PublishSubject

open class StubGravitySensor : GravitySensor {
    override val sensorDataStream: PublishSubject<Triple<Float, Float, Float>> =
        PublishSubject.create()

    override fun initialiseSensor(): Completable = Completable.complete()

    override fun register(): Completable = Completable.complete()
    override fun unregister() {}
}