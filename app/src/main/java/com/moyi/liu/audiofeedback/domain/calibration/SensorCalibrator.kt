package com.moyi.liu.audiofeedback.domain.calibration

import com.moyi.liu.audiofeedback.domain.model.CalibrationResult
import com.moyi.liu.audiofeedback.domain.model.Origin
import com.moyi.liu.audiofeedback.domain.sensor.GravitySensor
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SensorCalibrator @Inject constructor(
    private val gravitySensor: GravitySensor
) : Calibrator {

    override fun countDownAndPrepareSensor(
        countDownSeconds: Int,
        onTick: ((Int) -> Unit)?
    ): Completable =
        countdown(countDownSeconds, onTick)
            .flatMapCompletable {
                gravitySensor.initialiseSensor()
            }

    override fun startCalibration(
        countDownSeconds: Int,
        onTick: ((Int) -> Unit)?
    ): Maybe<CalibrationResult> {
        //Create a temporary `PublishSubject` in order to keep the `SensorDataStream` Subject alive
        //also when countdown terminates, pushes reduced result to downstream
        val tempSubject = PublishSubject.create<Triple<Float, Float, Float>>()
        val countdownMaybe = countdown(countDownSeconds, onTick).lastElement()
            .doOnTerminate { tempSubject.onComplete() }

        val countdownAndCalibration =
            Maybe.zip(countdownMaybe, tempSubject.startSensorDataCollection()) { _, data -> data }
                .doOnSubscribe {
                    gravitySensor.sensorDataStream.subscribe {
                        tempSubject.onNext(it)
                    }
                }

        return gravitySensor.register().andThen(countdownAndCalibration)
    }

    private fun Observable<Triple<Float, Float, Float>>.startSensorDataCollection(): Maybe<CalibrationResult> =
        this.map { it to 1 }
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

    private fun countdown(
        time: Int,
        onTick: ((Int) -> Unit)?
    ): Observable<Long> =
        Observable.interval(1, TimeUnit.SECONDS, Schedulers.io())
            .take(time.toLong())
            .doOnNext { onTick?.invoke(it.toInt() + 1) }


}