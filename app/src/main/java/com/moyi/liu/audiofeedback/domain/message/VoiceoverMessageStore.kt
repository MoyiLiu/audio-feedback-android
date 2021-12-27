package com.moyi.liu.audiofeedback.domain.message

import android.content.Context
import com.moyi.liu.audiofeedback.domain.model.CalibrationVoiceoverMessage
import com.moyi.liu.audiofeedback.domain.model.Message

class VoiceoverMessageStore(private val context: Context) : MessageStore {
    override fun getMessage(message: Message): String =
        when (message) {
            is CalibrationVoiceoverMessage.Preparation -> context.getString(
                message.stringRes,
                message.preparationTimeInSeconds
            )
            is CalibrationVoiceoverMessage.StartCalibration -> context.getString(
                message.stringRes,
                message.calibrationDurationInSeconds
            )
        }
}