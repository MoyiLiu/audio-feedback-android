package com.moyi.liu.audiofeedback.stub

import com.moyi.liu.audiofeedback.domain.message.MessageStore
import com.moyi.liu.audiofeedback.domain.model.CalibrationVoiceoverMessage
import com.moyi.liu.audiofeedback.domain.model.Message

class StubMessageStore : MessageStore {
    override fun getMessage(message: Message): String =
        when (message) {
            is CalibrationVoiceoverMessage.Preparation ->
                "preparation ${message.preparationTimeInSeconds}"
            is CalibrationVoiceoverMessage.StartCalibration ->
                "start calibration ${message.calibrationDurationInSeconds}"
        }
}