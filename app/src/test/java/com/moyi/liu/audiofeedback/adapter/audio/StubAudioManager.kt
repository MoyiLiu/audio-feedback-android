package com.moyi.liu.audiofeedback.adapter.audio

import com.moyi.liu.audiofeedback.domain.model.AudioContext
import com.moyi.liu.audiofeedback.domain.model.Direction
import io.reactivex.rxjava3.core.Completable

open class StubAudioManager : AudioManager {
    val audioContextsList = mutableListOf<Pair<AudioContext, AudioContext>>()
    var isStartLoopingTracksWithNoVolumeCalled = false
    var isReleaseAllTracksCalled = false
    var directions = mutableListOf<Direction>()

    override fun loadSoundTracks(): Completable = Completable.complete()

    override fun startLoopingTracksWithNoVolume() {
        isStartLoopingTracksWithNoVolumeCalled = true
    }

    override fun updateFrontBackTracks(audioContexts: Pair<AudioContext, AudioContext>) {
        audioContextsList.add(audioContexts)
    }

    override fun updateLeftRightTracks(direction: Direction) {
        directions.add(direction)
    }

    override fun releaseAllTracks() {
        isReleaseAllTracksCalled = true
    }
}