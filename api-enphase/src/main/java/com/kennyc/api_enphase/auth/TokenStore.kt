package com.kennyc.api_enphase.auth

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface TokenStore {

    fun saveTokens(accessToken: String, refreshToken: String, expiresAtEpochSec: Long): Completable

    fun clearTokens(): Completable

    fun accessTokenBlocking(): String?

    fun refreshTokenBlocking(): String?

    fun accessToken(): Single<String>

    fun refreshToken(): Single<String>

    fun expiresAtEpochSec(): Single<Long>

    fun storeOAuthState(state: String): Completable

    fun consumeOAuthState(): Single<String>
}
