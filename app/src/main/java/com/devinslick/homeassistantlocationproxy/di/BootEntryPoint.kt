package com.devinslick.homeassistantlocationproxy.di

import com.devinslick.homeassistantlocationproxy.data.SettingsRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface BootEntryPoint {
    fun settingsRepository(): SettingsRepository
}
