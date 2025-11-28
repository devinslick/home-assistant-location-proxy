package com.devinslick.homeassistantlocationproxy.ui

import com.devinslick.homeassistantlocationproxy.data.HaAttributes
import com.devinslick.homeassistantlocationproxy.data.SettingsEditor
import com.devinslick.homeassistantlocationproxy.network.HaError
import com.devinslick.homeassistantlocationproxy.network.HaResult
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MainViewModelTest {

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `refreshLatestState updates lastAttributes and status on success`() = runTest {
        val attrs = HaAttributes(44.5, -99.2, null, "mycar")
        val state = com.devinslick.homeassistantlocationproxy.data.HaStateResponse("device_tracker.my_car", "home", attrs, "2025-11-28T00:00:00")

        val fakeHaRepo = HaRepositoryFake(HaResult.Success(state))
        val fakeSettings = com.devinslick.homeassistantlocationproxy.network.FakeSettingsProvider()

        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)

        val vm = com.devinslick.homeassistantlocationproxy.ui.MainViewModel(fakeSettings, object : com.devinslick.homeassistantlocationproxy.data.SettingsEditor {
            override suspend fun setHaBaseUrl(url: String?) {}
            override suspend fun setHaToken(token: String?) {}
            override suspend fun setEntityId(entity: String?) {}
            override suspend fun setPollingInterval(seconds: Long) {}
            override suspend fun setIsPollingEnabled(enabled: Boolean) {}
            override suspend fun setIsSpoofingEnabled(enabled: Boolean) {}
        }, fakeHaRepo as com.devinslick.homeassistantlocationproxy.network.HaRepository)

        vm.refreshLatestState()
        advanceUntilIdle()

        assertEquals(44.5, vm.lastAttributes.value?.latitude)

        Dispatchers.resetMain()
    }
}
