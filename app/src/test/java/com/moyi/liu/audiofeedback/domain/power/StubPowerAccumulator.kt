package com.moyi.liu.audiofeedback.domain.power

import io.reactivex.rxjava3.subjects.PublishSubject

class StubPowerAccumulator : PowerAccumulator {
    override val chargeIndicator: PublishSubject<Unit> = PublishSubject.create()

    var isActivateCalled = false
    var isEmptyCalled = false
    var isDestroyCalled = false

    val charges = mutableListOf<Float>()

    override fun activate() {
        isActivateCalled = true
    }

    override fun chargeWith(power: Float) {
        charges.add(power)
    }

    override fun empty() {
        isEmptyCalled = true
    }

    override fun destroy() {
        isDestroyCalled = true
    }
}