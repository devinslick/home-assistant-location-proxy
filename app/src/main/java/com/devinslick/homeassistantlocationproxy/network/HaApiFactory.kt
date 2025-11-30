package com.devinslick.homeassistantlocationproxy.network

import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Provides a simple factory to create a `HaApiService` with a runtime base URL and token.
 *
 * We use `@Url` on endpoints or provide the entity endpoint relative to a baseUrl. The factory
 * will create a Retrofit instance for the provided baseUrl. If you call this often, consider
 * caching the retrofit instance.
 */
open class HaApiFactory(private val httpClient: OkHttpClient) {

    fun create(baseUrl: String, token: String?): HaApiService {
        val clientBuilder = httpClient.newBuilder()
        if (!token.isNullOrBlank()) {
            clientBuilder.addInterceptor { chain ->
                val req: Request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                chain.proceed(req)
            }
        }

        val retrofit = Retrofit.Builder()
            .baseUrl(normalizeBaseUrl(baseUrl))
            .client(clientBuilder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(HaApiService::class.java)
    }

    private fun normalizeBaseUrl(url: String): String {
        var normalized = url
        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            normalized = "http://$normalized"
        }
        // ensure Url ends with a trailing slash for Retrofit
        return if (!normalized.endsWith("/")) "$normalized/" else normalized
    }
}
