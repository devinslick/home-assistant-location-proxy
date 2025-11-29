package com.devinslick.homeassistantlocationproxy.data

import kotlinx.coroutines.flow.Flow

interface TokenStore {
    val tokenFlow: Flow<String?>
    suspend fun setToken(token: String?)
}
