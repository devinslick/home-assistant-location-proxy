package com.devinslick.homeassistantlocationproxy.data

import kotlinx.coroutines.flow.Flow

interface SettingsProvider {
    val haBaseUrl: Flow<String?>
    val haToken: Flow<String?>
    val entityId: Flow<String?>
    val pollingInterval: Flow<Long>
    val isPollingEnabled: Flow<Boolean>
    val isSpoofingEnabled: Flow<Boolean>
}
