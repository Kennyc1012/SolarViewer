package com.kennyc.data_enphase.auth

import android.net.Uri
import com.kennyc.api_enphase.auth.AuthApi
import com.kennyc.api_enphase.auth.TokenStore
import com.kennyc.solarviewer.data.AuthManager
import com.kennyc.solarviewer.data.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import okhttp3.Credentials
import java.security.SecureRandom
import androidx.core.net.toUri

class EnphaseAuthManager(
    private val authApi: AuthApi,
    private val tokenStore: TokenStore,
    private val clientId: String,
    private val clientSecret: String,
    private val redirectUri: String
) : AuthManager {

    private val _state = MutableStateFlow<AuthState>(AuthState.Unknown)
    override val state: StateFlow<AuthState> = _state.asStateFlow()

    override suspend fun refreshAuthState() {
        val token = tokenStore.accessToken().first()
        _state.value = if (token.isNotEmpty()) AuthState.Authorized else AuthState.Unauthorized
    }

    override suspend fun buildAuthorizeUri(): Uri {
        val state = randomState()
        tokenStore.storeOAuthState(state)
        return AUTHORIZE_URL.toUri().buildUpon()
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("client_id", clientId)
            .appendQueryParameter("redirect_uri", redirectUri)
            .appendQueryParameter("state", state)
            .build()
    }

    override fun isCallback(uri: Uri?): Boolean {
        if (uri == null) return false
        val expected = redirectUri.toUri()
        return uri.scheme == expected.scheme && uri.host == expected.host
    }

    override suspend fun handleCallback(uri: Uri) {
        val code = uri.getQueryParameter("code")
        val returnedState = uri.getQueryParameter("state")
        val error = uri.getQueryParameter("error")
        if (error != null) {
            _state.value = AuthState.Error(error)
            return
        }
        if (code.isNullOrEmpty() || returnedState.isNullOrEmpty()) {
            _state.value = AuthState.Error("missing code or state")
            return
        }
        val stored = tokenStore.consumeOAuthState()
        if (stored.isEmpty() || stored != returnedState) {
            _state.value = AuthState.Error("state mismatch")
            return
        }
        runCatching { exchangeCode(code) }
            .onFailure { _state.value = AuthState.Error(it.message ?: "exchange failed") }
    }

    private suspend fun exchangeCode(code: String) {
        val basic = Credentials.basic(clientId, clientSecret)
        val resp = authApi.exchangeCode(
            basicAuth = basic,
            code = code,
            redirectUri = redirectUri
        )
        val expiresAt = System.currentTimeMillis() / 1000L + resp.expiresIn
        tokenStore.saveTokens(resp.accessToken, resp.refreshToken, expiresAt)
        _state.value = AuthState.Authorized
    }

    override suspend fun signOut() {
        tokenStore.clearTokens()
        _state.value = AuthState.Unauthorized
    }

    private fun randomState(): String {
        val bytes = ByteArray(24)
        SecureRandom().nextBytes(bytes)
        return android.util.Base64.encodeToString(
            bytes,
            android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP or android.util.Base64.NO_PADDING
        )
    }

    private companion object {
        const val AUTHORIZE_URL = "https://api.enphaseenergy.com/oauth/authorize"
    }
}
