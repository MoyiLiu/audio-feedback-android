package com.moyi.liu.audiofeedback.domain.power

import com.moyi.liu.audiofeedback.domain.model.PowerAccumulatorConfig
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

class AFPowerAccumulatorTest {

    companion object {
        @BeforeClass
        @JvmStatic
        fun setup() {
            RxJavaPlugins.setIoSchedulerHandler {
                Schedulers.trampoline()
            }
        }

        @AfterClass
        @JvmStatic
        fun teardown() {
            RxJavaPlugins.reset()
        }
    }

    @Test
    fun givenEachPowerInputIsTenthOfTheCap_whenReceiveTenInputs_shouldEmitOneChargedSignal() {
        val acc =
            AFPowerAccumulator(PowerAccumulatorConfig(intakePerSecond = 20)).apply { activate() }

        val chargeIndicator = acc.chargeIndicator.test()

        for (i in 0 until 10) {
            acc.chargeWith(10f)
        }

        chargeIndicator.assertValueCount(1)
    }

    @Test
    fun givenPowerInputIsNotEnough_shouldNotEmitChargedSignal() {
        val acc = AFPowerAccumulator(PowerAccumulatorConfig(20)).apply { activate() }

        val chargeIndicator = acc.chargeIndicator.test()

        for (i in 0 until 5) {
            acc.chargeWith(10f)
        }

        chargeIndicator.assertValueCount(0)
    }

    @Test
    fun givenEachPowerInputIsOverCap_shouldEmitSameNumberOfSignalsAsTheNumberOfInputs() {
        val acc = AFPowerAccumulator(PowerAccumulatorConfig(20, 10f)).apply { activate() }

        val chargeIndicator = acc.chargeIndicator.test()

        for (i in 0 until 5) {
            acc.chargeWith(15f)
        }

        chargeIndicator.assertValueCount(5)
    }

    @Test
    fun givenInconsistentPowerInputs_shouldEmitTwoChargedSignals() {
        val acc = AFPowerAccumulator(PowerAccumulatorConfig(20, 10f)).apply { activate() }

        val chargeIndicator = acc.chargeIndicator.test()

        for (i in 0 until 2) {
            acc.chargeWith(9f)
            acc.chargeWith(15f)
        }

        chargeIndicator.assertValueCount(2)
    }

    @Test
    fun givenPowerAccumulatorIsHalfCharged_whenReceiveNaN_shouldIgnoreTheInput_andContinue() {
        val acc = AFPowerAccumulator(PowerAccumulatorConfig(20, 10f)).apply { activate() }

        val chargeIndicator = acc.chargeIndicator.test()

        acc.chargeWith(6f)
        acc.chargeWith(Float.NaN)
        acc.chargeWith(6f)

        chargeIndicator.assertValueCount(1)
    }

    @Test
    fun givenPowerAccumulatorIsAlmostCharged_whenReceiveEmptySignal_accumulatorIsEmptied() {
        val acc = AFPowerAccumulator(PowerAccumulatorConfig(20, 10f)).apply { activate() }

        val chargeIndicator = acc.chargeIndicator.test()

        acc.chargeWith(9f)
        acc.empty()
        acc.chargeWith(9f)

        chargeIndicator.assertValueCount(0)
    }
}