package com.devinslick.homeassistantlocationproxy.network

import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Provides a factory to create a `HaApiService` with a runtime base URL and token.
 *
 * The created service is cached and reused as long as the baseUrl and token are unchanged,
 * so OkHttp's connection pool is preserved across polling iterations.
 */
open class HaApiFactory(private val httpClient: OkHttpClient) {

    private var cachedBaseUrl: String? = null
    private var cachedToken: String? = null
    private var cachedService: HaApiService? = null

    @Synchronized
    fun create(baseUrl: String, token: String?): HaApiService {
        val normalizedUrl = normalizeBaseUrl(baseUrl)
        val existing = cachedService
        if (existing != null && normalizedUrl == cachedBaseUrl && token == cachedToken) {
            return existing
        }

        val clientBuilder = httpClient.newBuilder()
        if (!token.isNullOrBlank()) {
            clientBuilder.addInterceptor { chain ->
                val req: Request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                chain.proceed(req)
            }
        }

        val service = Retrofit.Builder()
            .baseUrl(normalizedUrl)
            .client(clientBuilder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HaApiService::class.java)

        cachedBaseUrl = normalizedUrl
        cachedToken = token
        cachedService = service
        return service
    }

    private fun normalizeBaseUrl(url: String): String {
        var normalized = url
        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            // Default to https:// — most HA instances support it and Android 9+ blocks plaintext.
            normalized = "https://$normalized"
        }
        return if (!normalized.endsWith("/")) "$normalized/" else normalized
    }
}
