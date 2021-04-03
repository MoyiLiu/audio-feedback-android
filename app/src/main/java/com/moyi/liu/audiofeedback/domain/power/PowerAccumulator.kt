package com.moyi.liu.audiofeedback.domain.power

import com.moyi.liu.audiofeedback.domain.model.PowerAccumulatorConfig
import com.moyi.liu.audiofeedback.utils.safeDispose
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject

interface PowerAccumulator {
    val chargeIndicator: PublishSubject<Unit>
    fun activate()
    fun chargeWith(power: Float)
    fun empty()
    fun destroy()
}

class AFPowerAccumulator(
    private val config: PowerAccumulatorConfig
) : PowerAccumulator {
    override val chargeIndicator: PublishSubject<Unit> = PublishSubject.create()

    private val accumulator: PublishSubject<Float> = PublishSubject.create()
    private var disposable: Disposable? = null

    override fun activate() {
        disposable.safeDispose()
        disposable = accumulator
            .observeOn(Schedulers.io())
            .scan(0f) { accumulator, power ->
                if (power.isNaN()) return@scan accumulator

                //receive signal of empty the accumulator
                if (power == Float.MIN_VALUE) return@scan 0f

                val successor = accumulator + power
                if (successor >= config.powerCap) {
                    chargeIndicator.onNext(Unit)
                    return@scan 0f
                }
                successor
            }
            .subscribe()
    }

    override fun chargeWith(power: Float) {
        accumulator.onNext(power)
    }

    override fun empty() {
        accumulator.onNext(Float.MIN_VALUE)
    }

    override fun destroy() {
        disposable.safeDispose()
    }
}