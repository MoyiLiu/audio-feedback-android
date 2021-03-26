package com.moyi.liu.audiofeedback.audio

import android.content.Context
import android.media.SoundPool
import com.moyi.liu.audiofeedback.R

interface AudioManager {

    fun loadSoundTracks()
    fun startLoopingTracksWithNoVolume()
    fun updateFrontBackTracks(audioStreamContext: AudioStreamContext)
}

class AFAudioManager (private val ctx: Context) : AudioManager {

    private var soundPool: SoundPool? = null
    private var frontTrackId: Int = 0
    private var backTrackId: Int = 0

    override fun loadSoundTracks() {
        soundPool = SoundPool.Builder().setMaxStreams(MAX_STREAMS).build()
        frontTrackId = soundPool?.load(ctx, R.raw.guitar_triplet_asc, 1) ?: 0
        backTrackId = soundPool?.load(ctx, R.raw.clarinet_c4 ,1) ?: 0
    }

    override fun startLoopingTracksWithNoVolume() {
        soundPool?.play(frontTrackId, 0f, 0f, 1, -1, 1f)
        soundPool?.play(backTrackId, 0f,0f, 1, -1, 1f)
    }

    override fun updateFrontBackTracks(audioStreamContext: AudioStreamContext) {
        soundPool?.setVolume(frontTrackId, audioStreamContext.frontVolume, audioStreamContext.frontVolume)
        soundPool?.setVolume(backTrackId, audioStreamContext.backVolume, audioStreamContext.backVolume)

    }

    companion object{
        const val MAX_STREAMS = 3
    }

}
