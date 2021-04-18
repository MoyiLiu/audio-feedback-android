package com.moyi.liu.audiofeedback.audio

import io.reactivex.rxjava3.core.Completable

interface VoiceoverController {
    fun initialise(): Completable
    fun speakOut(message: String, type: SpeechType)
    fun speakWith(message: String, type: SpeechType): Completable
    fun destroy()

    enum class SpeechType {
        IMMEDIATE, QUEUE
    }

    object InitialisationError : Exception()
    object QueuingError : Exception()
    object InterruptedError : Exception()
    object Error : Exception()
}