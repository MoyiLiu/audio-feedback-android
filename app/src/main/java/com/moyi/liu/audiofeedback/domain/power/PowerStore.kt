package com.moyi.liu.audiofeedback.domain.power

import com.moyi.liu.audiofeedback.domain.model.Direction
import io.reactivex.rxjava3.subjects.PublishSubject

interface PowerStore {
    val chargedIndicators: List<PublishSubject<Unit>>
    fun activate()
    fun shutdown()
    fun chargeWith(direction: Direction, power: Float)
    fun emptyWith(direction: Direction)
}

class AFPowerStore(
    private val leftPowerAccumulator: PowerAccumulator,
    private val rightPowerAccumulator: PowerAccumulator
) : PowerStore {

    override val chargedIndicators = listOf(
        leftPowerAccumulator.chargeIndicator,
        rightPowerAccumulator.chargeIndicator
    )

    override fun activate() {
        leftPowerAccumulator.activate()
        rightPowerAccumulator.activate()
    }

    override fun shutdown() {
        leftPowerAccumulator.destroy()
        rightPowerAccumulator.destroy()
    }

    override fun chargeWith(direction: Direction, power: Float) {
        when (direction) {
            Direction.LEFT -> leftPowerAccumulator.chargeWith(power)
            Direction.RIGHT -> rightPowerAccumulator.chargeWith(power)
        }
    }

    override fun emptyWith(direction: Direction) {
        when (direction) {
            Direction.LEFT -> leftPowerAccumulator.empty()
            Direction.RIGHT -> rightPowerAccumulator.empty()
        }
    }
}