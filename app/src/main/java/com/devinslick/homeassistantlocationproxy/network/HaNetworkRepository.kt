package com.devinslick.homeassistantlocationproxy.network

import android.util.Log
import com.devinslick.homeassistantlocationproxy.data.HaStateResponse
import com.devinslick.homeassistantlocationproxy.data.SettingsRepository
import kotlinx.coroutines.flow.first
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

sealed interface HaResult {
    data class Success(val state: HaStateResponse) : HaResult
    data class Failure(val error: HaError) : HaResult
}

sealed interface HaError {
    object Unauthorized : HaError
    object NotFound : HaError
    object MissingConfig : HaError
    data class Network(val cause: Throwable) : HaError
    data class Unknown(val code: Int?) : HaError
}

/**
 * Repository that coordinates calling the Home Assistant API using configured settings.
 *
 * This abstracts the Retrofit usage and maps responses into a small set of domain-friendly
 * results for the Consumers (Service, ViewModel, tests, etc.).
 */
@Singleton
class HaNetworkRepository @Inject constructor(
    private val apiFactory: HaApiFactory,
    private val settings: com.devinslick.homeassistantlocationproxy.data.SettingsProvider
) {

    private val logTag = "HaNetworkRepository"

    /**
     * Attempt to fetch the HA entity state using current settings (baseUrl, token, entityId).
     * Returns either Success(state) or Failure(HaError).
     */
    suspend fun fetchEntityState(): HaResult {
        // Read settings once (suspending) to ensure consistency per call
        val baseUrl = settings.haBaseUrl.first()
        val token = settings.haToken.first()
        val entityId = settings.entityId.first()

        if (baseUrl.isNullOrBlank() || entityId.isNullOrBlank()) {
            Log.w(logTag, "Missing configuration - baseUrl: $baseUrl, entityId: $entityId")
            return HaResult.Failure(HaError.MissingConfig)
        }

        val api = apiFactory.create(baseUrl, token)

        try {
            val response = api.getState(entityId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    return HaResult.Success(body)
                } else {
                    Log.w(logTag, "Empty response body for entityId: $entityId")
                    return HaResult.Failure(HaError.Unknown(response.code()))
                }
            } else {
                when (response.code()) {
                    401 -> return HaResult.Failure(HaError.Unauthorized)
                    404 -> return HaResult.Failure(HaError.NotFound)
                    else -> {
                        Log.w(logTag, "HA API error ${response.code()} for entityId: $entityId")
                        return HaResult.Failure(HaError.Unknown(response.code()))
                    }
                }
            }
        } catch (e: IOException) {
            Log.w(logTag, "Network error when calling HA API", e)
            return HaResult.Failure(HaError.Network(e))
        } catch (t: Throwable) {
            Log.w(logTag, "Unexpected error when calling HA API", t)
            return HaResult.Failure(HaError.Network(t))
        }
    }
}
