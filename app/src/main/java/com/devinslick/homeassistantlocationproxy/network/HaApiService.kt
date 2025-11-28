package com.devinslick.homeassistantlocationproxy.network

import com.devinslick.homeassistantlocationproxy.data.HaStateResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface HaApiService {
    @GET("/api/states/{entity_id}")
    suspend fun getState(@Path("entity_id") entityId: String): Response<HaStateResponse>
}
