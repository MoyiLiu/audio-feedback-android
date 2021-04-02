package com.moyi.liu.audiofeedback.domain

import com.moyi.liu.audiofeedback.utils.safeDispose
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject

class PowerAccumulator(
    private val powerCap: Float
) {
    val chargeIndicator: PublishSubject<Unit> = PublishSubject.create()
    private val accumulator: PublishSubject<Float> = PublishSubject.create()
    private var disposable: Disposable? = null

    fun activate() {
        disposable.safeDispose()
        disposable = accumulator
            .observeOn(Schedulers.io())
            .scan(0f) { accumulator, power ->
                if (power.isNaN()) return@scan accumulator

                val successor = accumulator + power
                if (successor >= powerCap) {
                    chargeIndicator.onNext(Unit)
                    return@scan 0f
                }
                successor
            }
            .subscribe()
    }

    fun chargeWith(power: Float) {
        accumulator.onNext(power)
    }

    fun destroy() {
        disposable.safeDispose()
    }
}