package com.moyi.liu.audiofeedback.domain.calibration

import com.moyi.liu.audiofeedback.domain.model.CalibrationResult
import com.moyi.liu.audiofeedback.domain.model.Origin
import com.moyi.liu.audiofeedback.domain.sensor.GravitySensor
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SensorCalibrator @Inject constructor(
    private val gravitySensor: GravitySensor
) : Calibrator {

    override fun countDownAndPrepareSensor(
        countDownSeconds: Int,
        onTick: ((Int) -> Unit)?
    ): Completable =
        countDown(countDownSeconds, onTick)
            .flatMapCompletable {
                gravitySensor.initialiseSensor()
            }

    override fun startCalibration(
        countDownSeconds: Int,
        onTick: ((Int) -> Unit)?
    ): Maybe<CalibrationResult> {
        val countdownMaybe = countDown(countDownSeconds, onTick).lastElement()
            .doOnTerminate { gravitySensor.sensorDataStream.onComplete() }

        val countdownAndCalibration =
            Maybe.zip(countdownMaybe, startSensorDataCollection()) { _, data -> data }

        return gravitySensor.register().andThen(countdownAndCalibration)
    }

    private fun startSensorDataCollection(): Maybe<CalibrationResult> =
        gravitySensor.sensorDataStream
            .map { it to 1 }
            .reduce { (accTriple, counter), (itemTriple, count) ->
                val (xa, ya, za) = accTriple
                val (x, y, z) = itemTriple
                Triple(xa + x, ya + y, za + z) to counter + count
            }
            .map { (sum, count) ->
                CalibrationResult(
                    origin = Origin(
                        sum.first / count, sum.second / count, sum.third / count
                    ),
                    numberOfDataPoints = count
                )
            }

    private fun countDown(
        time: Int,
        onTick: ((Int) -> Unit)?
    ): Observable<Long> =
        Observable.interval(1, TimeUnit.SECONDS, Schedulers.io())
            .take(time.toLong())
            .doOnNext { onTick?.invoke(it.toInt() + 1) }


}