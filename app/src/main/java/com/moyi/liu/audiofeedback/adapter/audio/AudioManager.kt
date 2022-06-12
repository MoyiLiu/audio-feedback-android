package com.moyi.liu.audiofeedback.adapter.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioAttributes.*
import android.media.SoundPool
import com.moyi.liu.audiofeedback.R
import com.moyi.liu.audiofeedback.domain.model.AudioContext
import com.moyi.liu.audiofeedback.domain.model.Direction
import com.moyi.liu.audiofeedback.adapter.transformer.MAX_VOLUME
import com.moyi.liu.audiofeedback.adapter.transformer.MIN_VOLUME
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable

interface AudioManager {

    fun loadSoundTracks(): Completable
    fun startLoopingTracksWithNoVolume()
    fun updateFrontBackTracks(audioContexts: Pair<AudioContext, AudioContext>)
    fun updateLeftRightTracks(direction: Direction)
    fun releaseAllTracks()

}

class AFAudioManager(private val ctx: Context) : AudioManager {

    private var soundPool: SoundPool? = null
    private var frontTrackId: Int = 0
    private var backTrackId: Int = 0
    private var leftRightTrackId: Int = 0

    override fun loadSoundTracks(): Completable =
        Completable.create { emitter ->
            soundPool = SoundPool.Builder()
                .setMaxStreams(MAX_STREAMS)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(USAGE_ASSISTANCE_ACCESSIBILITY)
                        .setContentType(CONTENT_TYPE_SONIFICATION)
                        .setFlags(FLAG_AUDIBILITY_ENFORCED)
                        .build()
                )
                .build()
            frontTrackId = soundPool?.load(ctx, R.raw.guitar_triplet_asc, 1) ?: Int.MIN_VALUE
            backTrackId = soundPool?.load(ctx, R.raw.clarinet_c4, 1) ?: Int.MIN_VALUE
            leftRightTrackId = soundPool?.load(ctx, R.raw.beep, 1) ?: Int.MIN_VALUE

            if (frontTrackId == Int.MIN_VALUE) emitter.onError(FailedToLoadSoundTrackException)
            if (backTrackId == Int.MIN_VALUE) emitter.onError(FailedToLoadSoundTrackException)
            if (leftRightTrackId == Int.MIN_VALUE) emitter.onError(FailedToLoadSoundTrackException)

            emitter.onComplete()
        }.subscribeOn(AndroidSchedulers.mainThread())

    override fun startLoopingTracksWithNoVolume() {
        soundPool?.play(frontTrackId, MIN_VOLUME, MIN_VOLUME, 1, -1, 1f)
        soundPool?.play(backTrackId, MIN_VOLUME, MIN_VOLUME, 1, -1, 1f)
    }

    override fun updateFrontBackTracks(audioContexts: Pair<AudioContext, AudioContext>) {
        val (front, back) = audioContexts
        soundPool?.setVolume(frontTrackId, front.volume, front.volume)
        soundPool?.setVolume(backTrackId, back.volume, back.volume)

        soundPool?.setRate(frontTrackId, front.playRate)
        soundPool?.setRate(backTrackId, back.playRate)
    }

    override fun updateLeftRightTracks(direction: Direction) {
        when (direction) {
            Direction.LEFT -> {
                soundPool?.play(leftRightTrackId, MAX_VOLUME, MIN_VOLUME, 1, 0, 1f)
            }
            Direction.RIGHT -> {
                soundPool?.play(leftRightTrackId, MIN_VOLUME, MAX_VOLUME, 1, 0, 1f)
            }
        }
    }

    override fun releaseAllTracks() {
        soundPool?.release()
    }

    companion object {
        const val MAX_STREAMS = 3
    }

}

object FailedToLoadSoundTrackException : Exception()