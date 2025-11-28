package com.devinslick.homeassistantlocationproxy.network

import com.devinslick.homeassistantlocationproxy.data.HaStateResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Path

class FakeHaApiFactory(private val response: Response<HaStateResponse>? = null, private val throwable: Throwable? = null) : HaApiFactory(FakeOkHttpClient()) {

    override fun create(baseUrl: String, token: String?): HaApiService = object : HaApiService {
        override suspend fun getState(@Path("entity_id") entityId: String): Response<HaStateResponse> {
            throwable?.let { throw throwable }
            return response ?: Response.error(401, ResponseBody.create("text/plain".toMediaTypeOrNull(), "Unauthorized"))
        }
    }
}

// Simple stub to satisfy the superclass; doesn't actually work, but fine for testing since we override create().
class FakeOkHttpClient : okhttp3.OkHttpClient()
