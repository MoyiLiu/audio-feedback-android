package com.moyi.liu.audiofeedback.domain

import androidx.annotation.VisibleForTesting
import com.moyi.liu.audiofeedback.adapter.audio.AudioManager
import com.moyi.liu.audiofeedback.domain.model.Direction
import com.moyi.liu.audiofeedback.domain.power.PowerStore
import com.moyi.liu.audiofeedback.domain.sensor.GravitySensor
import com.moyi.liu.audiofeedback.adapter.transformer.SensorDataTransformer
import com.moyi.liu.audiofeedback.utils.safeDispose
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject

class AudioFeedbackHandler(
    private val sensor: GravitySensor,
    private val audioManager: AudioManager,
    private val dataTransformer: SensorDataTransformer,
    private val powerStore: PowerStore
) {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal var dataStreamDisposable: Disposable? = null

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal var powerInputStreamDisposable: Disposable? = null

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal var leftChargedIndicatorStreamDisposable: Disposable? = null

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal var rightChargedIndicatorStreamDisposable: Disposable? = null


    private val powerInputStream = PublishSubject.create<Pair<Direction, Float>>()

    /**
     * Load sound files into memory (SoundPool) and initialise the sensor
     */
    fun setup(): Completable =
        audioManager.loadSoundTracks()
            .mergeWith(sensor.initialiseSensor())
            .subscribeOn(AndroidSchedulers.mainThread())

    /**
     * Prep work
     * - Register the sensor for receiving data
     * - Setup power stream to allow sending power values to [PowerAccumulator]s
     * - Setup charged indicator stream to receive the power fully-charged signal from [PowerAccumulator]s
     * - Start looping some tracks silently
     */
    fun start(): Completable = sensor.register()
        .doOnSubscribe {
            subscribePowerStream()
            subscribeChargedIndicators()
            audioManager.startLoopingTracksWithNoVolume()
        }
        .doOnComplete(::subscribeDataStream)
        .doOnError { terminate() }
        .subscribeOn(AndroidSchedulers.mainThread())

    /**
     * Cleanup cached resources and close streams
     */
    fun terminate() {
        dataStreamDisposable.safeDispose()
        powerInputStreamDisposable.safeDispose()
        leftChargedIndicatorStreamDisposable.safeDispose()
        rightChargedIndicatorStreamDisposable.safeDispose()
        powerStore.shutdown()
        audioManager.releaseAllTracks()
    }

    /**
     * Setup sensor data stream to react on it by transforming to domain models
     * and indicate [PowerStore] and update [AudioManager]
     */
    private fun subscribeDataStream() {
        dataStreamDisposable.safeDispose()
        dataStreamDisposable = sensor.sensorDataStream
            .observeOn(Schedulers.io())
            .map { (x, _, z) ->
                dataTransformer.run {
                    transformForFrontBackTracks(z) to transformForLeftRightTracks(x)
                }
            }
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { (audioContexts, powerContext) ->
                    audioManager.updateFrontBackTracks(audioContexts)
                    powerInputStream.onNext(powerContext)
                }, {
                    //ignore Error
                }
            )
    }

    /**
     * Setup power stream and process the data on:
     * - Direction change - empty opposite direction [PowerAccumulator]
     * - Charge the [PowerAccumulator] of the received direction
     */
    private fun subscribePowerStream() {
        powerInputStreamDisposable.safeDispose()
        powerInputStreamDisposable = powerInputStream
            .observeOn(Schedulers.io())
            .scan(Pair(Direction.LEFT, 0f)) { pre, newPowerContext ->
                val (preDirection, _) = pre
                val (newDirection, _) = newPowerContext
                if (preDirection != newDirection) {
                    when (newDirection) {
                        Direction.LEFT -> powerStore.emptyWith(Direction.RIGHT)
                        Direction.RIGHT -> powerStore.emptyWith(Direction.LEFT)
                    }
                }
                newPowerContext
            }
            .doOnSubscribe { powerStore.activate() }
            .subscribe({ (direction, power) ->
                powerStore.chargeWith(direction, power)
            }, {
                powerStore.shutdown()
            })
    }

    /**
     * Setup charged indicator streams which make [AudioManager] to play a signal note once on one signal receive
     */
    private fun subscribeChargedIndicators() {
        leftChargedIndicatorStreamDisposable.safeDispose()
        rightChargedIndicatorStreamDisposable.safeDispose()
        leftChargedIndicatorStreamDisposable = powerStore.chargedIndicators[0]
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { audioManager.updateLeftRightTracks(Direction.LEFT) }, {
                    //ignore error
                })

        rightChargedIndicatorStreamDisposable = powerStore.chargedIndicators[1]
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { audioManager.updateLeftRightTracks(Direction.RIGHT) }, {
                    //ignore error
                })
    }
}