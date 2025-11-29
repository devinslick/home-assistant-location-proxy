package com.devinslick.homeassistantlocationproxy.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TokenStoreInstrumentedTest {
    @Test
    fun tokenIsPersistedAndReadViaFlow() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val masterKey = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
        val prefs = EncryptedSharedPreferences.create(context, "test_secure_prefs", masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)

        val store = EncryptedTokenStore(prefs)

        // Start fresh
        store.setToken(null)

        // Initially should be null
        assertEquals(null, store.tokenFlow.first())

        store.setToken("instrumented-token")
        assertEquals("instrumented-token", store.tokenFlow.first())
    }
}
