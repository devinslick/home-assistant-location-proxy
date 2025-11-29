package com.devinslick.homeassistantlocationproxy.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tokenStore: TokenStore
) : SettingsProvider, SettingsEditor {

    companion object {
        private val KEY_HA_BASE_URL = stringPreferencesKey("ha_base_url")
        private val KEY_HA_TOKEN = stringPreferencesKey("ha_token")
        // HA token is now stored in EncryptedSharedPreferences via TokenStore
        private val KEY_ENTITY_ID = stringPreferencesKey("target_entity_id")
        private val KEY_POLLING_INTERVAL = longPreferencesKey("polling_interval_seconds")
        private val KEY_POLLING_ENABLED = booleanPreferencesKey("is_polling_enabled")
        private val KEY_SPOOFING_ENABLED = booleanPreferencesKey("is_spoofing_enabled")

        private const val DEFAULT_POLLING_INTERVAL = 30L
    }

    private val ds = context.dataStore
    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        // Migrate token from DataStore if present to the secure TokenStore and remove it from DataStore.
        repoScope.launch {
            val tokenFromDs = ds.data.map { prefs -> prefs[KEY_HA_TOKEN] }.first()
            if (!tokenFromDs.isNullOrBlank()) {
                tokenStore.setToken(tokenFromDs)
                ds.edit { prefs -> prefs.remove(KEY_HA_TOKEN) }
            }
        }
    }

    override val haBaseUrl: Flow<String?> = ds.data.map { prefs -> prefs[KEY_HA_BASE_URL] }
    override val haToken: Flow<String?> = tokenStore.tokenFlow
    override val entityId: Flow<String?> = ds.data.map { prefs -> prefs[KEY_ENTITY_ID] }
    override val pollingInterval: Flow<Long> = ds.data.map { prefs -> prefs[KEY_POLLING_INTERVAL] ?: DEFAULT_POLLING_INTERVAL }
    override val isPollingEnabled: Flow<Boolean> = ds.data.map { prefs -> prefs[KEY_POLLING_ENABLED] ?: false }
    override val isSpoofingEnabled: Flow<Boolean> = ds.data.map { prefs -> prefs[KEY_SPOOFING_ENABLED] ?: false }

    override suspend fun setHaBaseUrl(url: String?) {
        ds.edit { prefs ->
            if (url.isNullOrBlank()) prefs.remove(KEY_HA_BASE_URL) else prefs[KEY_HA_BASE_URL] = url
        }
    }

    override suspend fun setHaToken(token: String?) {
        tokenStore.setToken(token)
    }

    override suspend fun setEntityId(entity: String?) {
        ds.edit { prefs ->
            if (entity.isNullOrBlank()) prefs.remove(KEY_ENTITY_ID) else prefs[KEY_ENTITY_ID] = entity
        }
    }

    override suspend fun setPollingInterval(seconds: Long) {
        ds.edit { prefs ->
            prefs[KEY_POLLING_INTERVAL] = seconds
        }
    }

    override suspend fun setIsPollingEnabled(enabled: Boolean) {
        ds.edit { prefs ->
            prefs[KEY_POLLING_ENABLED] = enabled
        }
    }

    override suspend fun setIsSpoofingEnabled(enabled: Boolean) {
        ds.edit { prefs ->
            prefs[KEY_SPOOFING_ENABLED] = enabled
        }
    }
}
