package com.moyi.liu.audiofeedback.adapter.audio

import io.reactivex.rxjava3.core.Completable

interface VoiceoverController {
    /** Initialise Voiceover Engine */
    fun initialise(): Completable
    /** Attempt to speak out the message regardless the results */
    fun speakOut(message: String, type: SpeechType = SpeechType.QUEUE)
    /** Attempt to speak out the message with a result response */
    fun speakWith(message: String, type: SpeechType = SpeechType.QUEUE, timeoutMillis: Long = 3000L): Completable
    /** Clean up the resources */
    fun destroy()

    enum class SpeechType {
        IMMEDIATE, QUEUE
    }

    object InitialisationError : Exception()
    object QueuingError : Exception()
    object InterruptedError : Exception()
    object Error : Exception()
}