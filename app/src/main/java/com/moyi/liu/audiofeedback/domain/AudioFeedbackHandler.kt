package com.moyi.liu.audiofeedback.domain

import androidx.annotation.VisibleForTesting
import com.moyi.liu.audiofeedback.audio.AudioManager
import com.moyi.liu.audiofeedback.sensor.GravitySensor
import com.moyi.liu.audiofeedback.transformer.SensorDataTransformer
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers

class AudioFeedbackHandler(
    private val sensor: GravitySensor,
    private val audioManager: AudioManager,
    private val dataTransformer: SensorDataTransformer
) {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var dataStreamDisposable: Disposable? = null

    fun setup(): Completable =
        audioManager.loadSoundTracks()
            .mergeWith(sensor.initialiseSensor())

    fun start(): Completable {
        if (dataStreamDisposable != null && dataStreamDisposable?.isDisposed == false)
            dataStreamDisposable?.dispose()

        return sensor.register()
            .doOnSubscribe {
                audioManager.startLoopingTracksWithNoVolume()
            }
            .doOnComplete(::subscribeDataStream)
            .doOnError {
                //TODO cleanup audio manager
            }
            .subscribeOn(AndroidSchedulers.mainThread())
    }


    private fun subscribeDataStream() {
        dataStreamDisposable = sensor.sensorDataStream
            .observeOn(Schedulers.io())
            .map(dataTransformer::transformForFrontBackTracks)
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { audioContexts ->
                    audioManager.updateFrontBackTracks(audioContexts)
                }, {
                    //ignore Error
                }
            )
    }
}