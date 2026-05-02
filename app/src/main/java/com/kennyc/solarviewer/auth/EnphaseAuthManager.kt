package com.kennyc.solarviewer.auth

import android.app.Activity
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.kennyc.api_enphase.auth.AuthApi
import com.kennyc.api_enphase.auth.TokenStore
import com.kennyc.solarviewer.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import okhttp3.Credentials
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthState {
    data object Unknown : AuthState()
    data object Unauthorized : AuthState()
    data object Authorized : AuthState()
    data class Error(val message: String) : AuthState()
}

@Singleton
class EnphaseAuthManager @Inject constructor(
    private val authApi: AuthApi,
    private val tokenStore: TokenStore
) {

    private val _state = MutableStateFlow<AuthState>(AuthState.Unknown)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    suspend fun refreshAuthState() {
        val token = tokenStore.accessToken().first()
        _state.value = if (token.isNotEmpty()) AuthState.Authorized else AuthState.Unauthorized
    }

    suspend fun startAuthorize(activity: Activity) {
        val state = randomState()
        tokenStore.storeOAuthState(state)
        val url = Uri.parse(AUTHORIZE_URL).buildUpon()
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("client_id", BuildConfig.CLIENT_ID)
            .appendQueryParameter("redirect_uri", BuildConfig.REDIRECT_URI)
            .appendQueryParameter("state", state)
            .build()
        CustomTabsIntent.Builder().build().launchUrl(activity, url)
    }

    fun isCallback(uri: Uri?): Boolean {
        if (uri == null) return false
        val expected = Uri.parse(BuildConfig.REDIRECT_URI)
        return uri.scheme == expected.scheme && uri.host == expected.host
    }

    suspend fun handleCallback(uri: Uri) {
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
        val basic = Credentials.basic(BuildConfig.CLIENT_ID, BuildConfig.CLIENT_SECRET)
        val resp = authApi.exchangeCode(
            basicAuth = basic,
            code = code,
            redirectUri = BuildConfig.REDIRECT_URI
        )
        val expiresAt = System.currentTimeMillis() / 1000L + resp.expiresIn
        tokenStore.saveTokens(resp.accessToken, resp.refreshToken, expiresAt)
        _state.value = AuthState.Authorized
    }

    suspend fun signOut() {
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
