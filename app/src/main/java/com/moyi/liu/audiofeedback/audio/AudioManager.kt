package com.moyi.liu.audiofeedback.audio

import android.content.Context
import android.media.SoundPool
import com.moyi.liu.audiofeedback.R
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import java.lang.Exception
import kotlin.jvm.Throws

interface AudioManager {

    fun loadSoundTracks(): Completable
    fun startLoopingTracksWithNoVolume()
    fun updateFrontBackTracks(audioContexts: Pair<AudioContext, AudioContext>)
    fun releaseAllTracks()

}

class AFAudioManager(private val ctx: Context) : AudioManager {

    private var soundPool: SoundPool? = null
    private var frontTrackId: Int = 0
    private var backTrackId: Int = 0

    override fun loadSoundTracks(): Completable =
        Completable.create { emitter ->
            soundPool = SoundPool.Builder().setMaxStreams(MAX_STREAMS).build()
            frontTrackId = soundPool?.load(ctx, R.raw.guitar_triplet_asc, 1) ?: Int.MIN_VALUE
            backTrackId = soundPool?.load(ctx, R.raw.clarinet_c4, 1) ?: Int.MIN_VALUE

            if (frontTrackId == Int.MIN_VALUE) emitter.onError(FailedToLoadSoundTrackException)
            if (backTrackId == Int.MIN_VALUE) emitter.onError(FailedToLoadSoundTrackException)

            emitter.onComplete()
        }.subscribeOn(AndroidSchedulers.mainThread())

    override fun startLoopingTracksWithNoVolume() {
        soundPool?.play(frontTrackId, 0f, 0f, 1, -1, 1f)
        soundPool?.play(backTrackId, 0f, 0f, 1, -1, 1f)
    }

    override fun updateFrontBackTracks(audioContexts: Pair<AudioContext, AudioContext>) {
        val (front, back) = audioContexts
        soundPool?.setVolume(frontTrackId, front.volume, front.volume)
        soundPool?.setVolume(backTrackId, back.volume, back.volume)

        soundPool?.setRate(frontTrackId, front.playRate)
        soundPool?.setRate(backTrackId, back.playRate)
    }

    override fun releaseAllTracks() {
        soundPool?.release()
    }

    companion object {
        const val MAX_STREAMS = 3
    }

}

object FailedToLoadSoundTrackException : Exception()