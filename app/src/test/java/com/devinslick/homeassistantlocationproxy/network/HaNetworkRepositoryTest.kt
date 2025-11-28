package com.devinslick.homeassistantlocationproxy.network

import com.devinslick.homeassistantlocationproxy.data.HaAttributes
import com.devinslick.homeassistantlocationproxy.data.HaStateResponse
import com.devinslick.homeassistantlocationproxy.network.HaError
import com.devinslick.homeassistantlocationproxy.network.HaNetworkRepository
import com.devinslick.homeassistantlocationproxy.network.HaResult
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class HaNetworkRepositoryTest {

    @Test
    fun `fetchEntityState returns success on successful response`() = runBlocking {
        val attrs = HaAttributes(44.5, -99.2, null, "mycar")
        val state = HaStateResponse("device_tracker.my_car", "home", attrs, "2025-11-28T00:00:00")

        val fakeResponse = Response.success(state)
        val fakeFactory = FakeHaApiFactory(response = fakeResponse)
        val fakeSettings = FakeSettingsProvider()

        val repo = HaNetworkRepository(fakeFactory, fakeSettings)
        val result = repo.fetchEntityState()

        assertTrue(result is HaResult.Success)
        val s = result as HaResult.Success
        assertEquals(44.5, s.state.attributes.latitude)
    }

    @Test
    fun `fetchEntityState returns unauthorized on 401 response`() = runBlocking {
        val fakeResponse = Response.error<HaStateResponse>(401, ResponseBody.create("text/plain".toMediaTypeOrNull(), "Unauthorized"))
        val fakeFactory = FakeHaApiFactory(response = fakeResponse)
        val fakeSettings = FakeSettingsProvider()

        val repo = HaNetworkRepository(fakeFactory, fakeSettings)
        val result = repo.fetchEntityState()

        assertTrue(result is HaResult.Failure)
        val failure = result as HaResult.Failure
        assertTrue(failure.error is HaError.Unauthorized)
    }

    @Test
    fun `fetchEntityState returns missing config when base url not set`() = runBlocking {
        val fakeFactory = FakeHaApiFactory()
        val fakeSettings = FakeSettingsProvider(baseUrl = null)

        val repo = HaNetworkRepository(fakeFactory, fakeSettings)
        val result = repo.fetchEntityState()

        assertTrue(result is HaResult.Failure)
        assertTrue((result as HaResult.Failure).error is HaError.MissingConfig)
    }

    @Test
    fun `fetchEntityState maps network exceptions to HaError Network`() = runBlocking {
        val fakeFactory = FakeHaApiFactory(throwable = java.io.IOException("timeout"))
        val fakeSettings = FakeSettingsProvider()

        val repo = HaNetworkRepository(fakeFactory, fakeSettings)
        val result = repo.fetchEntityState()

        assertTrue(result is HaResult.Failure)
        val failure = result as HaResult.Failure
        assertTrue(failure.error is HaError.Network)
    }
}
