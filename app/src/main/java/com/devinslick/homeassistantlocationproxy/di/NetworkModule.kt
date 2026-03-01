package com.devinslick.homeassistantlocationproxy.di

import com.devinslick.homeassistantlocationproxy.BuildConfig
import com.devinslick.homeassistantlocationproxy.network.HaApiFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)

        // Only attach the logging interceptor in debug builds to avoid log noise in production
        // and to prevent accidental leakage of request headers (e.g. Authorization tokens)
        // if the log level is ever increased to HEADERS or BODY.
        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC)
            builder.addInterceptor(logging)
        }

        return builder.build()
    }

    @Provides
    @Singleton
    fun provideHaApiFactory(client: OkHttpClient): HaApiFactory = HaApiFactory(client)
}
