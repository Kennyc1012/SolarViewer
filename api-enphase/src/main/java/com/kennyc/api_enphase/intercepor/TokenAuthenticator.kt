package com.kennyc.api_enphase.intercepor

import com.kennyc.api_enphase.auth.AuthApi
import com.kennyc.api_enphase.auth.TokenStore
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Credentials
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val authApi: AuthApi,
    private val tokenStore: TokenStore,
    private val clientId: String,
    private val clientSecret: String
) : Authenticator {

    private val lock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null

        val staleToken = response.request.header("Authorization")?.removePrefix("Bearer ")

        synchronized(lock) {
            val current = tokenStore.accessTokenBlocking()
            if (current != null && current != staleToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $current")
                    .build()
            }

            val refresh = tokenStore.refreshTokenBlocking() ?: return null
            val basic = Credentials.basic(clientId, clientSecret)

            val refreshed = runCatching {
                runBlocking { authApi.refresh(basic, refreshToken = refresh) }
            }.getOrElse {
                runBlocking { tokenStore.clearTokens() }
                return null
            }

            val expiresAt = System.currentTimeMillis() / 1000L + refreshed.expiresIn
            runBlocking {
                tokenStore.saveTokens(refreshed.accessToken, refreshed.refreshToken, expiresAt)
            }

            return response.request.newBuilder()
                .header("Authorization", "Bearer ${refreshed.accessToken}")
                .build()
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
