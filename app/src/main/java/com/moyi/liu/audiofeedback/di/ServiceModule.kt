package com.moyi.liu.audiofeedback.di

import android.app.Service
import android.content.Context
import com.moyi.liu.audiofeedback.adapter.audio.AudioManager
import com.moyi.liu.audiofeedback.domain.AudioFeedbackHandler
import com.moyi.liu.audiofeedback.domain.sensor.GravitySensor
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.scopes.ServiceScoped
import javax.inject.Qualifier

@InstallIn(ServiceComponent::class)
@Module
class AudioFeedbackModule {

    @ServiceScoped
    @Provides
    fun provideAudioFeedbackHandler(
        sensor: GravitySensor,
        audioManager: AudioManager
    ): AudioFeedbackHandler = AudioFeedbackHandler(sensor, audioManager)
}


@InstallIn(ServiceComponent::class)
@Module
abstract class ServiceBindingModule {
    @Binds
    @ServiceContext
    abstract fun bindServiceContext(service: Service): Context

}

@Qualifier
annotation class ServiceContext