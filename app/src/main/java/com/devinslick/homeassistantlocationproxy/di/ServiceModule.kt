package com.devinslick.homeassistantlocationproxy.di

import com.devinslick.homeassistantlocationproxy.service.ForegroundServiceController
import com.devinslick.homeassistantlocationproxy.service.ServiceController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton
import android.content.Context

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {
    @Provides
    @Singleton
    fun provideServiceController(@ApplicationContext context: Context): ServiceController {
        return ForegroundServiceController(context)
    }
}
