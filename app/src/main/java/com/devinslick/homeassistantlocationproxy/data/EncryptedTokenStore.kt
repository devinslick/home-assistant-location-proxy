package com.devinslick.homeassistantlocationproxy.data

import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class EncryptedTokenStore @Inject constructor(private val prefs: SharedPreferences) : TokenStore {
    companion object {
        const val KEY_HA_TOKEN = "ha_token"
    }

    override val tokenFlow: Flow<String?> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == KEY_HA_TOKEN) {
                trySend(sharedPreferences.getString(KEY_HA_TOKEN, null))
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        // Emit current value immediately
        trySend(prefs.getString(KEY_HA_TOKEN, null))

        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    override suspend fun setToken(token: String?) {
        // Use commit() (synchronous) so callers can trust the write is durable before returning.
        // apply() is async and risks losing data if the process dies before the write completes.
        withContext(Dispatchers.IO) {
            prefs.edit().run {
                if (token == null) remove(KEY_HA_TOKEN) else putString(KEY_HA_TOKEN, token)
                commit()
            }
        }
    }
}
