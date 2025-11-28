package com.devinslick.homeassistantlocationproxy.ui

import com.devinslick.homeassistantlocationproxy.data.HaStateResponse
import com.devinslick.homeassistantlocationproxy.network.HaError
import com.devinslick.homeassistantlocationproxy.network.HaNetworkRepository
import com.devinslick.homeassistantlocationproxy.network.HaResult

class FakeHaNetworkRepository(private val result: HaResult) : HaNetworkRepositoryFakeBase() {
    override suspend fun fetchEntityState(): HaResult {
        return result
    }
}

// We introduce a small base so tests can easily implement this fake repository.
open class HaNetworkRepositoryFakeBase : HaNetworkRepository(
    apiFactory = com.devinslick.homeassistantlocationproxy.network.HaApiFactory(okhttp3.OkHttpClient()),
    settings = com.devinslick.homeassistantlocationproxy.data.SettingsRepository(com.example.locationproxy.LocationProxyApp())
) {
    // Not used in tests; we override fetchEntityState in Fake
    override suspend fun fetchEntityState(): HaResult {
        return HaResult.Failure(HaError.MissingConfig)
    }
}
