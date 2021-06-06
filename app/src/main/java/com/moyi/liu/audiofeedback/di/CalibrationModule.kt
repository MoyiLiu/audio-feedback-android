package com.moyi.liu.audiofeedback.di

import android.content.Context
import com.moyi.liu.audiofeedback.domain.audio.AFVoiceoverController
import com.moyi.liu.audiofeedback.domain.audio.VoiceoverController
import com.moyi.liu.audiofeedback.domain.calibration.Calibrator
import com.moyi.liu.audiofeedback.domain.calibration.SensorCalibrator
import com.moyi.liu.audiofeedback.domain.message.MessageStore
import com.moyi.liu.audiofeedback.domain.message.VoiceoverMessageStore
import com.moyi.liu.audiofeedback.domain.model.CalibrationConfig
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.scopes.ServiceScoped

@InstallIn(ServiceComponent::class)
@Module
class CalibrationModule {
    @Provides
    @ServiceScoped
    fun provideAFVoiceController(
        @ServiceContext ctx: Context
    ): VoiceoverController = AFVoiceoverController(ctx)

    //TODO: Get config values from preference
    @Provides
    fun provideCalibrationConfig(): CalibrationConfig =
        CalibrationConfig(
            preparationTimeInSeconds = 5,
            calibrationDurationInSeconds = 5
        )

    @Provides
    @ServiceScoped
    fun provideVoiceoverMessageStore(
        @ServiceContext ctx: Context
    ): MessageStore = VoiceoverMessageStore(ctx)
}

@InstallIn(ServiceComponent::class)
@Module
abstract class CalibrationBindingModule {
    @Binds
    @ServiceScoped
    abstract fun bindSensorCalibrator(sensorCalibrator: SensorCalibrator): Calibrator
}