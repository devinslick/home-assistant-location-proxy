package com.devinslick.homeassistantlocationproxy.network

import com.devinslick.homeassistantlocationproxy.data.HaStateResponse

interface HaRepository {
    suspend fun fetchEntityState(): HaResult
}
