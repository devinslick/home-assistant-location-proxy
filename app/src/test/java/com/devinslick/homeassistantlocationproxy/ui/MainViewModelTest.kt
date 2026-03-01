package com.devinslick.homeassistantlocationproxy.ui

import com.devinslick.homeassistantlocationproxy.data.HaAttributes
import com.devinslick.homeassistantlocationproxy.data.HaStateResponse
import com.devinslick.homeassistantlocationproxy.data.SettingsEditor
import com.devinslick.homeassistantlocationproxy.network.FakeSettingsProvider
import com.devinslick.homeassistantlocationproxy.network.HaResult
import com.devinslick.homeassistantlocationproxy.permissions.PermissionChecker
import com.devinslick.homeassistantlocationproxy.service.ServiceController
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Test

/** No-op [ServiceController] for use in unit tests. */
private val noOpServiceController = object : ServiceController {
    override fun startService() {}
    override fun stopService() {}
}

/** No-op [SettingsEditor] for use in unit tests. */
private val noOpSettingsEditor = object : SettingsEditor {
    override suspend fun setHaBaseUrl(url: String?) {}
    override suspend fun setHaToken(token: String?) {}
    override suspend fun setEntityId(entity: String?) {}
    override suspend fun setPollingInterval(seconds: Long) {}
    override suspend fun setIsPollingEnabled(enabled: Boolean) {}
    override suspend fun setIsSpoofingEnabled(enabled: Boolean) {}
}

class MainViewModelTest {

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `refreshLatestState updates lastAttributes and status on success`() = runTest {
        val attrs = HaAttributes(44.5, -99.2, null, "mycar")
        val state = HaStateResponse("device_tracker.my_car", "home", attrs, "2025-11-28T00:00:00")

        val fakeHaRepo = FakeHaNetworkRepository(HaResult.Success(state))
        val fakeSettings = FakeSettingsProvider()
        val mockPermissionChecker = mockk<PermissionChecker>(relaxed = true)

        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)

        val vm = MainViewModel(
            settings = fakeSettings,
            settingsEditor = noOpSettingsEditor,
            haRepository = fakeHaRepo,
            permissionChecker = mockPermissionChecker,
            serviceController = noOpServiceController
        )

        vm.refreshLatestState()
        advanceUntilIdle()

        assertEquals(44.5, vm.lastAttributes.value?.latitude)

        Dispatchers.resetMain()
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `service starts when polling is enabled and stops when disabled`() = runTest {
        val fakeSettings = FakeSettingsProvider(isPollingEnabled = false)
        val mockPermissionChecker = mockk<PermissionChecker>(relaxed = true)
        val mockServiceController = mockk<ServiceController>(relaxed = true)
        val fakeHaRepo = FakeHaNetworkRepository(HaResult.Failure(com.devinslick.homeassistantlocationproxy.network.HaError.MissingConfig))

        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)

        val vm = MainViewModel(
            settings = fakeSettings,
            settingsEditor = noOpSettingsEditor,
            haRepository = fakeHaRepo,
            permissionChecker = mockPermissionChecker,
            serviceController = mockServiceController
        )

        // Initial state: polling is disabled — stopService() should be called
        advanceUntilIdle()
        verify { mockServiceController.stopService() }

        // Enable polling — startService() should be called
        fakeSettings.setIsPollingEnabled(true)
        advanceUntilIdle()
        verify { mockServiceController.startService() }

        Dispatchers.resetMain()
    }
}
