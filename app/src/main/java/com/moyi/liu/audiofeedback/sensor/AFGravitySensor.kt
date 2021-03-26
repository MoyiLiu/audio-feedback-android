package com.moyi.liu.audiofeedback.sensor

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.subjects.PublishSubject

class AFGravitySensor(
    private val systemGravitySensor: SystemGravitySensor
) : GravitySensor {
    override val sensorDataStream: PublishSubject<Triple<Float, Float, Float>> =
        PublishSubject.create()

    override fun initialiseSensor(): Completable = Completable.create { emitter ->
        try {
            systemGravitySensor.initialise()
        } catch (e: Exception) {
            emitter.onError(e)
        }
        emitter.onComplete()
    }.subscribeOn(AndroidSchedulers.mainThread())

    override fun register(): Completable =
        Completable.create { emitter ->
            systemGravitySensor.register { x, y, z ->
                sensorDataStream.onNext(Triple(x, y, z))
            }

            emitter.onComplete()
        }.subscribeOn(AndroidSchedulers.mainThread())

    override fun unregister() {
        systemGravitySensor.unregister()
    }

}