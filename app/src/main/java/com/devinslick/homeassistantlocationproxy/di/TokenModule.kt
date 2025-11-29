package com.devinslick.homeassistantlocationproxy.di

import android.content.SharedPreferences
import com.devinslick.homeassistantlocationproxy.data.EncryptedTokenStore
import com.devinslick.homeassistantlocationproxy.data.TokenStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TokenModule {
    @Provides
    @Singleton
    fun provideTokenStore(prefs: SharedPreferences): TokenStore {
        return EncryptedTokenStore(prefs)
    }
}
