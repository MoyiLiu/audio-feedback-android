package com.moyi.liu.audiofeedback.domain.model

import androidx.annotation.StringRes
import com.moyi.liu.audiofeedback.R

sealed class Message(@StringRes open val stringRes: Int)

sealed class CalibrationVoiceoverMessage(
    @StringRes override val stringRes: Int
) : Message(stringRes) {

    class Preparation(val preparationTimeInSeconds: Int) :
        CalibrationVoiceoverMessage(R.string.voiceover_calibration_preparation_message)

    class StartCalibration(val calibrationDurationInSeconds: Int) :
        CalibrationVoiceoverMessage(R.string.voiceover_calibration_start_calibration_message)
}
