package com.kennyc.api_enphase.auth

import kotlinx.coroutines.flow.Flow

interface TokenStore {

    suspend fun saveTokens(accessToken: String, refreshToken: String, expiresAtEpochSec: Long)

    suspend fun clearTokens()

    /** Synchronous snapshot read for OkHttp interceptor. Backed by an in-memory cache. */
    fun accessTokenBlocking(): String?

    /** Synchronous snapshot read for OkHttp authenticator. Backed by an in-memory cache. */
    fun refreshTokenBlocking(): String?

    fun accessToken(): Flow<String>

    fun refreshToken(): Flow<String>

    fun expiresAtEpochSec(): Flow<Long>

    suspend fun storeOAuthState(state: String)

    suspend fun consumeOAuthState(): String
}
