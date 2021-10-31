package com.moyi.liu.audiofeedback.domain.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.CompletableEmitter
import java.util.*
import java.util.concurrent.TimeUnit

class AFVoiceoverController(
    private val context: Context
) : VoiceoverController, UtteranceProgressListener() {

    private var tts: TextToSpeech? = null
    private val speeches = mutableMapOf<String, CompletableEmitter>()

    override fun initialise(): Completable =
        Completable.create { emitter ->
            tts = TextToSpeech(context) { status ->
                when (status) {
                    TextToSpeech.SUCCESS -> emitter.onComplete()
                    else -> {
                        tts = null
                        emitter.onError(VoiceoverController.InitialisationError)
                    }
                }
            }
        }
            .timeout(2, TimeUnit.SECONDS)
            .doOnComplete {
                tts?.run {
                    language = Locale.UK
                    setSpeechRate(1.3f)
                    setOnUtteranceProgressListener(this@AFVoiceoverController)
                }
            }

    override fun speakOut(message: String, type: VoiceoverController.SpeechType) {
        tts?.speak(message, type.toTextToSpeechType(), null, message.toUtteranceId())
    }

    override fun speakWith(
        message: String,
        type: VoiceoverController.SpeechType,
        timeoutMillis: Long
    ): Completable = Completable.create { emitter ->
        val id = message.toUtteranceId()
        val queueResult =
            tts?.speak(message, type.toTextToSpeechType(), null, id)

        if (queueResult == TextToSpeech.SUCCESS) {
            speeches[id]?.onError(VoiceoverController.InterruptedError)
            speeches[message.toUtteranceId()] = emitter
        } else {
            emitter.onError(VoiceoverController.QueuingError)
        }
    }.timeout(timeoutMillis, TimeUnit.MILLISECONDS)

    override fun destroy() {
        for (e in speeches.values) {
            if(!e.isDisposed)
                e.onError(VoiceoverController.InterruptedError)
        }
        speeches.clear()

        tts?.stop()
        tts?.shutdown()
    }

    override fun onDone(utteranceId: String?) {
        utteranceId?.let { id ->
            speeches[id]?.onComplete()
            speeches.remove(id)
        }
    }

    override fun onError(utteranceId: String?) {
        utteranceId?.let { id ->
            speeches[id]?.onError(VoiceoverController.Error)
            speeches.remove(id)
        }
    }

    override fun onStart(utteranceId: String?) {}

    private fun VoiceoverController.SpeechType.toTextToSpeechType(): Int =
        when (this) {
            VoiceoverController.SpeechType.IMMEDIATE -> TextToSpeech.QUEUE_FLUSH
            VoiceoverController.SpeechType.QUEUE -> TextToSpeech.QUEUE_ADD
        }

    private fun String.toUtteranceId() = this.hashCode().toString()

}