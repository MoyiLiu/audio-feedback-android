package com.moyi.liu.audiofeedback.domain.usecase

import com.moyi.liu.audiofeedback.domain.audio.VoiceoverController
import com.moyi.liu.audiofeedback.domain.calibration.Calibrator
import com.moyi.liu.audiofeedback.domain.message.MessageStore
import com.moyi.liu.audiofeedback.domain.model.CalibrationConfig
import com.moyi.liu.audiofeedback.domain.model.CalibrationResult
import com.moyi.liu.audiofeedback.domain.model.CalibrationVoiceoverMessage
import io.reactivex.rxjava3.core.Single
import java.lang.IllegalArgumentException
import javax.inject.Inject

class CalibrationUseCase @Inject constructor(
    private val messageStore: MessageStore,
    private val calibrator: Calibrator,
    private val voiceoverController: VoiceoverController,
    private val calibrationConfig: CalibrationConfig
) {
    fun startCalibration(): Single<CalibrationResult> =
        voiceoverController.initialise()
            .andThen(
                voiceoverController.speakWith(getPreparationMessage())
            )
            .andThen(
                calibrator.countDownAndPrepareSensor(calibrationConfig.preparationTimeInSeconds) {
                    val second = calibrationConfig.preparationTimeInSeconds - it
                    voiceoverController.speakOut(second.toString())
                }.doOnSubscribe {
                    voiceoverController.speakOut(calibrationConfig.preparationTimeInSeconds.toString())
                }
            ).andThen(
                voiceoverController.speakWith(getStartCalibrationMessage())
            ).andThen(
                calibrator.startCalibration(calibrationConfig.calibrationDurationInSeconds) {
                    val second = calibrationConfig.calibrationDurationInSeconds - it
                    voiceoverController.speakOut(second.toString())
                }.doOnSubscribe {
                    voiceoverController.speakOut(calibrationConfig.calibrationDurationInSeconds.toString())
                }
            ).toSingle()
            .doAfterTerminate {
                voiceoverController.destroy()
            }

    private fun getPreparationMessage(): String = messageStore.getMessage(
        CalibrationVoiceoverMessage.Preparation(calibrationConfig.preparationTimeInSeconds)
    ) ?: throw IllegalArgumentException("Empty voiceover calibration preparation message")

    private fun getStartCalibrationMessage(): String = messageStore.getMessage(
        CalibrationVoiceoverMessage.StartCalibration(calibrationConfig.calibrationDurationInSeconds)
    ) ?: throw IllegalArgumentException("Empty voiceover start calibration message")
}