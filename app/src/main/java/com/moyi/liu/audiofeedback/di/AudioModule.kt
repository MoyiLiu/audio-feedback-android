package com.moyi.liu.audiofeedback.di

import android.content.Context
import com.moyi.liu.audiofeedback.adapter.audio.AFAudioManager
import com.moyi.liu.audiofeedback.adapter.audio.AudioManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.scopes.ServiceScoped

@InstallIn(ServiceComponent::class)
@Module
class AudioModule {
    @ServiceScoped
    @Provides
    fun provideAFAudioManager(
        @ServiceContext ctx: Context
    ): AudioManager = AFAudioManager(ctx)
}