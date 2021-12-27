package com.moyi.liu.audiofeedback.stub

import com.moyi.liu.audiofeedback.domain.audio.VoiceoverController
import io.reactivex.rxjava3.core.Completable

class StubVoiceoverController : VoiceoverController {
    val speakMessages = mutableListOf<String>()
    var isDestroyCalled = false
    var isInitialiseCalled = false

    override fun initialise(): Completable = Completable.create {
        isInitialiseCalled = true
        it.onComplete()
    }

    override fun speakOut(message: String, type: VoiceoverController.SpeechType) {
        speakMessages.add(message)
    }

    override fun speakWith(
        message: String,
        type: VoiceoverController.SpeechType,
        timeoutMillis: Long
    ): Completable = Completable.create {
        speakMessages.add(message)
        it.onComplete()
    }

    override fun destroy() {
        isDestroyCalled = true
    }

}