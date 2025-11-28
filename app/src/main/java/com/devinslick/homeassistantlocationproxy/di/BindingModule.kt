package com.devinslick.homeassistantlocationproxy.di

import com.devinslick.homeassistantlocationproxy.network.HaNetworkRepository
import com.devinslick.homeassistantlocationproxy.network.HaRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BindingModule {
    @Binds
    @Singleton
    abstract fun bindHaRepository(impl: HaNetworkRepository): HaRepository
}
