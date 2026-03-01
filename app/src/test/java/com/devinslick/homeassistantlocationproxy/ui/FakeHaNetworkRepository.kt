package com.devinslick.homeassistantlocationproxy.ui

import com.devinslick.homeassistantlocationproxy.network.HaRepository
import com.devinslick.homeassistantlocationproxy.network.HaResult

class FakeHaNetworkRepository(private val result: HaResult) : HaRepository {
    override suspend fun fetchEntityState(): HaResult = result
}
