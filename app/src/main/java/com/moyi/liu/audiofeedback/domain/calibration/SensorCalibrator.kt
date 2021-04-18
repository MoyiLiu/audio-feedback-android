package com.moyi.liu.audiofeedback.domain.calibration

import com.moyi.liu.audiofeedback.domain.sensor.GravitySensor
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class SensorCalibrator(private val gravitySensor: GravitySensor) {

    fun countDownAndPrepareSensor(
        countDownSeconds: Long,
        onTick: ((Long) -> Unit)? = null
    ): Completable =
        countDown(countDownSeconds, onTick)
            .flatMapCompletable {
                gravitySensor.initialiseSensor()
            }

    fun startCalibration(
        countDownSeconds: Long,
        onTick: ((Long) -> Unit)? = null
    ): Maybe<Pair<Triple<Float, Float, Float>, Int>> {
        val countdownMaybe = countDown(countDownSeconds, onTick).lastElement()
            .doOnTerminate { gravitySensor.sensorDataStream.onComplete() }

        val countdownAndCalibration =
            Maybe.zip(countdownMaybe, startSensorDataCollection()) { _, data -> data }

        return gravitySensor.register().andThen(countdownAndCalibration)
    }

    private fun startSensorDataCollection(): Maybe<Pair<Triple<Float, Float, Float>, Int>> =
        gravitySensor.sensorDataStream
            .map { it to 1 }
            .reduce { (accTriple, counter), (itemTriple, count) ->
                val (xa, ya, za) = accTriple
                val (x, y, z) = itemTriple
                Triple(xa + x, ya + y, za + z) to counter + count
            }
            .map { (sum, count) ->
                Triple(sum.first / count, sum.second / count, sum.third / count) to count
            }

    private fun countDown(
        time: Long,
        onTick: ((Long) -> Unit)?
    ): Observable<Long> =
        Observable.interval(1, TimeUnit.SECONDS, Schedulers.io())
            .take(time)
            .doOnNext { onTick?.invoke(it + 1) }


}