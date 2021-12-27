package com.moyi.liu.audiofeedback.di

import android.content.Context
import com.moyi.liu.audiofeedback.adapter.sensor.AFSystemGravitySensor
import com.moyi.liu.audiofeedback.adapter.sensor.SystemGravitySensor
import com.moyi.liu.audiofeedback.domain.sensor.AFGravitySensor
import com.moyi.liu.audiofeedback.domain.sensor.GravitySensor
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.scopes.ServiceScoped

@InstallIn(ServiceComponent::class)
@Module
class SensorModule {

    @ServiceScoped
    @Provides
    fun provideSystemGravitySensor(
        @ServiceContext ctx: Context
    ): SystemGravitySensor = AFSystemGravitySensor(ctx)

    @ServiceScoped
    @Provides
    fun provideAFGravitySensor(
        systemGravitySensor: SystemGravitySensor
    ): GravitySensor = AFGravitySensor(systemGravitySensor)
}