package com.kennyc.api_enphase.intercepor

import com.kennyc.api_enphase.auth.AuthApi
import com.kennyc.api_enphase.auth.TokenStore
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
            val refreshResponse = runCatching {
                authApi.refresh(basic, refreshToken = refresh).execute()
            }.getOrNull() ?: return null

            if (!refreshResponse.isSuccessful) {
                tokenStore.clearTokens().blockingAwait()
                return null
            }

            val body = refreshResponse.body() ?: return null
            val expiresAt = System.currentTimeMillis() / 1000L + body.expiresIn
            tokenStore.saveTokens(body.accessToken, body.refreshToken, expiresAt)
                .blockingAwait()

            return response.request.newBuilder()
                .header("Authorization", "Bearer ${body.accessToken}")
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
