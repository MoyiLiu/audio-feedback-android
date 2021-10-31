package com.moyi.liu.audiofeedback.domain

import androidx.annotation.VisibleForTesting
import com.moyi.liu.audiofeedback.adapter.audio.AudioManager
import com.moyi.liu.audiofeedback.adapter.transformer.SensorDataTransformer
import com.moyi.liu.audiofeedback.domain.model.Boundary
import com.moyi.liu.audiofeedback.domain.model.Direction
import com.moyi.liu.audiofeedback.domain.model.PowerAccumulatorConfig
import com.moyi.liu.audiofeedback.domain.power.AFPowerAccumulator
import com.moyi.liu.audiofeedback.domain.power.AFPowerStore
import com.moyi.liu.audiofeedback.domain.power.PowerAccumulator
import com.moyi.liu.audiofeedback.domain.power.PowerStore
import com.moyi.liu.audiofeedback.domain.sensor.GravitySensor
import com.moyi.liu.audiofeedback.domain.usecase.CalibrationUseCase
import com.moyi.liu.audiofeedback.utils.safeDispose
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import java.lang.IllegalStateException

class AudioFeedbackHandler(
    private val sensor: GravitySensor,
    private val audioManager: AudioManager,
    private val calibrationUseCase: CalibrationUseCase
) {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal var dataStreamDisposable: Disposable? = null

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal var powerInputStreamDisposable: Disposable? = null

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal var leftChargedIndicatorStreamDisposable: Disposable? = null

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal var rightChargedIndicatorStreamDisposable: Disposable? = null

    val isPowerStoreInitialised: Boolean = this::_powerStore.isInitialized
    val isDataTransformerInitialised: Boolean = this::_dataTransformer.isInitialized

    var powerStore: PowerStore
        get() = _powerStore
        set(value) {
            if (this::_powerStore.isInitialized) throw IllegalStateException("PowerStore is initialised.")
            else _powerStore = value
        }

    var dataTransformer: SensorDataTransformer
        get() = _dataTransformer
        set(value) {
            if (this::_dataTransformer.isInitialized) throw IllegalStateException("PowerStore is initialised.")
            else _dataTransformer = value
        }

    private lateinit var _powerStore: PowerStore
    private lateinit var _dataTransformer: SensorDataTransformer


    private val powerInputStream = PublishSubject.create<Pair<Direction, Float>>()

    /**
     * Load sound files into memory (SoundPool) and initialise the sensor
     */
    fun setup(): Completable =
        audioManager.loadSoundTracks()
            .mergeWith(sensor.initialiseSensor())
            .subscribeOn(AndroidSchedulers.mainThread())

    fun calibrate(): Completable =
        calibrationUseCase.startCalibration()
            .doOnSuccess { (origin, numOfDataPoints) ->
                val powerAccumulatorConfig = PowerAccumulatorConfig(
                    intakePerSecond = numOfDataPoints / calibrationUseCase.calibrationConfig.calibrationDurationInSeconds,
                )
                _powerStore = AFPowerStore(
                    leftPowerAccumulator = AFPowerAccumulator(powerAccumulatorConfig),
                    rightPowerAccumulator = AFPowerAccumulator(powerAccumulatorConfig)
                )
                _dataTransformer = SensorDataTransformer(
                    frontBackAxisOriginValue = origin.x,
                    leftRightAxisOriginValue = origin.y,
                    //TODO check angles
                    frontBackBoundaries = Boundary(14f, 24f) to Boundary(10f, 20f),
                    leftRightBoundaries = Boundary(10f, 30f) to Boundary(10f, 30f),
                    accumulatorConfig = powerAccumulatorConfig
                )
            }
            .subscribeOn(Schedulers.io())
            .ignoreElement()

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
        _powerStore.shutdown()
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
                _dataTransformer.run {
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
                        Direction.LEFT -> _powerStore.emptyWith(Direction.RIGHT)
                        Direction.RIGHT -> _powerStore.emptyWith(Direction.LEFT)
                    }
                }
                newPowerContext
            }
            .doOnSubscribe { _powerStore.activate() }
            .subscribe({ (direction, power) ->
                _powerStore.chargeWith(direction, power)
            }, {
                _powerStore.shutdown()
            })
    }

    /**
     * Setup charged indicator streams which make [AudioManager] to play a signal note once on one signal receive
     */
    private fun subscribeChargedIndicators() {
        leftChargedIndicatorStreamDisposable.safeDispose()
        rightChargedIndicatorStreamDisposable.safeDispose()
        leftChargedIndicatorStreamDisposable = _powerStore.chargedIndicators[0]
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { audioManager.updateLeftRightTracks(Direction.LEFT) }, {
                    //ignore error
                })

        rightChargedIndicatorStreamDisposable = _powerStore.chargedIndicators[1]
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { audioManager.updateLeftRightTracks(Direction.RIGHT) }, {
                    //ignore error
                })
    }
}