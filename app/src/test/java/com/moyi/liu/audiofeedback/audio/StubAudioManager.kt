package com.moyi.liu.audiofeedback.audio

import io.reactivex.rxjava3.core.Completable

open class StubAudioManager : AudioManager {
    val audioContextsList = mutableListOf<Pair<AudioContext, AudioContext>>()
    var isStartLoopingTracksWithNoVolumeCalled = false
    var isReleaseAllTracksCalled = false
    override fun loadSoundTracks(): Completable = Completable.complete()

    override fun startLoopingTracksWithNoVolume() {
        isStartLoopingTracksWithNoVolumeCalled = true
    }

    override fun updateFrontBackTracks(audioContexts: Pair<AudioContext, AudioContext>) {
        audioContextsList.add(audioContexts)
    }

    override fun releaseAllTracks() {
        isReleaseAllTracksCalled = true
    }
}