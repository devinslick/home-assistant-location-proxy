package com.devinslick.homeassistantlocationproxy.network

import com.devinslick.homeassistantlocationproxy.data.SettingsProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSettingsProvider(
    baseUrl: String? = "https://fake.local:8123",
    token: String? = "fake-token",
    entityId: String? = "device_tracker.my_car",
    pollingInterval: Long = 30L,
    isPollingEnabled: Boolean = false,
    isSpoofingEnabled: Boolean = false
) : SettingsProvider {

    private val _baseUrl = MutableStateFlow(baseUrl)
    private val _token = MutableStateFlow(token)
    private val _entityId = MutableStateFlow(entityId)
    private val _pollingInterval = MutableStateFlow(pollingInterval)
    private val _isPolling = MutableStateFlow(isPollingEnabled)
    private val _isSpoofing = MutableStateFlow(isSpoofingEnabled)

    override val haBaseUrl: Flow<String?> = _baseUrl
    override val haToken: Flow<String?> = _token
    override val entityId: Flow<String?> = _entityId
    override val pollingInterval: Flow<Long> = _pollingInterval
    override val isPollingEnabled: Flow<Boolean> = _isPolling
    override val isSpoofingEnabled: Flow<Boolean> = _isSpoofing

    fun setBaseUrl(value: String?) {
        _baseUrl.value = value
    }

    fun setEntityId(value: String?) {
        _entityId.value = value
    }

    fun setToken(value: String?) {
        _token.value = value
    }

}
