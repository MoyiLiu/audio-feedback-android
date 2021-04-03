package com.moyi.liu.audiofeedback.domain.power

import com.moyi.liu.audiofeedback.domain.model.Direction
import io.reactivex.rxjava3.subjects.PublishSubject

class StubPowerStore(
    override val chargedIndicators: List<PublishSubject<Unit>> = emptyList()
) : PowerStore {
    var isActivateCalled = false
    var isShutdownCalled = false
    val emptiedDirections = mutableListOf<Direction>()
    val chargedList = mutableListOf<Pair<Direction, Float>>()

    override fun activate() {
        isActivateCalled = true
    }

    override fun shutdown() {
        isShutdownCalled = true
    }

    override fun chargeWith(direction: Direction, power: Float) {
        chargedList.add(direction to power)
    }

    override fun emptyWith(direction: Direction) {
        emptiedDirections.add(direction)
    }
}

fun getStubChargedIndicators(numberOfIndicators: Int): List<PublishSubject<Unit>> {
    val list = mutableListOf<PublishSubject<Unit>>()
    for (i in 0 until numberOfIndicators) {
        list.add(PublishSubject.create())
    }
    return list
}
