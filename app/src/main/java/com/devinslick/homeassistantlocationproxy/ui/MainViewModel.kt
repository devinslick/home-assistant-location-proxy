package com.devinslick.homeassistantlocationproxy.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devinslick.homeassistantlocationproxy.data.HaAttributes
import com.devinslick.homeassistantlocationproxy.permissions.PermissionChecker
import com.devinslick.homeassistantlocationproxy.data.SettingsRepository
import com.devinslick.homeassistantlocationproxy.network.HaNetworkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settings: com.devinslick.homeassistantlocationproxy.data.SettingsProvider,
    private val settingsEditor: com.devinslick.homeassistantlocationproxy.data.SettingsEditor,
    private val haRepository: com.devinslick.homeassistantlocationproxy.network.HaRepository,
    private val permissionChecker: PermissionChecker
) : ViewModel() {

    private val _isPollingEnabled = MutableStateFlow(false)
    val isPollingEnabled: StateFlow<Boolean> = _isPollingEnabled.asStateFlow()

    private val _isSpoofingEnabled = MutableStateFlow(false)
    val isSpoofingEnabled: StateFlow<Boolean> = _isSpoofingEnabled.asStateFlow()

    private val _haBaseUrl = MutableStateFlow<String?>(null)
    val haBaseUrl: StateFlow<String?> = _haBaseUrl.asStateFlow()

    private val _haToken = MutableStateFlow<String?>(null)
    val haToken: StateFlow<String?> = _haToken.asStateFlow()

    private val _entityId = MutableStateFlow<String?>(null)
    val entityId: StateFlow<String?> = _entityId.asStateFlow()

    private val _pollingInterval = MutableStateFlow(30L)
    val pollingInterval: StateFlow<Long> = _pollingInterval.asStateFlow()

    private val _lastAttributes = MutableStateFlow<HaAttributes?>(null)
    val lastAttributes: StateFlow<HaAttributes?> = _lastAttributes.asStateFlow()

    private val _statusMessage = MutableStateFlow("Stopped")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    private val _hasLocationPermission = MutableStateFlow(false)
    val hasLocationPermission: StateFlow<Boolean> = _hasLocationPermission.asStateFlow()

    private val _hasNotificationPermission = MutableStateFlow(false)
    val hasNotificationPermission: StateFlow<Boolean> = _hasNotificationPermission.asStateFlow()

    private val _isMockLocationApp = MutableStateFlow(false)
    val isMockLocationApp: StateFlow<Boolean> = _isMockLocationApp.asStateFlow()

    init {
        viewModelScope.launch {
            settings.haBaseUrl.collect { _haBaseUrl.value = it }
        }
        viewModelScope.launch {
            settings.haToken.collect { _haToken.value = it }
        }
        viewModelScope.launch {
            settings.entityId.collect { _entityId.value = it }
        }
        viewModelScope.launch {
            settings.pollingInterval.collect { _pollingInterval.value = it }
        }
        viewModelScope.launch {
            settings.isPollingEnabled.collect { _isPollingEnabled.value = it }
        }
        viewModelScope.launch {
            settings.isSpoofingEnabled.collect { _isSpoofingEnabled.value = it }
        }
        viewModelScope.launch {
            refreshPermissions()
        }
    }

    fun setPollingEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsEditor.setIsPollingEnabled(enabled) }
    }

    fun setSpoofingEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsEditor.setIsSpoofingEnabled(enabled) }
    }

    fun updateBaseUrl(url: String?) {
        viewModelScope.launch { settingsEditor.setHaBaseUrl(url) }
    }

    fun updateToken(token: String?) {
        viewModelScope.launch { settingsEditor.setHaToken(token) }
    }

    fun updateEntityId(entity: String?) {
        viewModelScope.launch { settingsEditor.setEntityId(entity) }
    }

    fun updatePollingInterval(seconds: Long) {
        viewModelScope.launch { settingsEditor.setPollingInterval(seconds) }
    }

    fun refreshLatestState() {
        viewModelScope.launch {
            _statusMessage.value = "Refreshing..."
            when (val r = haRepository.fetchEntityState()) {
                is com.devinslick.homeassistantlocationproxy.network.HaResult.Success -> {
                    _lastAttributes.value = r.state.attributes
                    _statusMessage.value = "OK"
                }
                is com.devinslick.homeassistantlocationproxy.network.HaResult.Failure -> {
                    _statusMessage.value = when (r.error) {
                        is com.devinslick.homeassistantlocationproxy.network.HaError.MissingConfig -> "Missing Config"
                        is com.devinslick.homeassistantlocationproxy.network.HaError.Unauthorized -> "Unauthorized"
                        is com.devinslick.homeassistantlocationproxy.network.HaError.NotFound -> "Not Found"
                        is com.devinslick.homeassistantlocationproxy.network.HaError.Network -> "Network Error"
                        is com.devinslick.homeassistantlocationproxy.network.HaError.Unknown -> "Unknown Error"
                    }
                }
            }
        }
    }

    fun refreshPermissions() {
        _hasLocationPermission.value = permissionChecker.hasFineLocationPermission()
        _hasNotificationPermission.value = permissionChecker.hasNotificationPermission()
        _isMockLocationApp.value = permissionChecker.isMockLocationAppSelected()
    }
}
