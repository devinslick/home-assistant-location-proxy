package com.devinslick.homeassistantlocationproxy.data

interface SettingsEditor {
    suspend fun setHaBaseUrl(url: String?)
    suspend fun setHaToken(token: String?)
    suspend fun setEntityId(entity: String?)
    suspend fun setPollingInterval(seconds: Long)
    suspend fun setIsPollingEnabled(enabled: Boolean)
    suspend fun setIsSpoofingEnabled(enabled: Boolean)
}
