package com.kennyc.solarviewer.auth

import android.app.Activity
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.kennyc.api_enphase.auth.AuthApi
import com.kennyc.api_enphase.auth.TokenStore
import com.kennyc.solarviewer.BuildConfig
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.BehaviorSubject
import okhttp3.Credentials
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthState {
    object Unknown : AuthState()
    object Unauthorized : AuthState()
    object Authorized : AuthState()
    data class Error(val message: String) : AuthState()
}

@Singleton
class EnphaseAuthManager @Inject constructor(
    private val authApi: AuthApi,
    private val tokenStore: TokenStore
) {

    private val stateSubject = BehaviorSubject.createDefault<AuthState>(AuthState.Unknown)
    val state: io.reactivex.rxjava3.core.Observable<AuthState> = stateSubject.hide()

    fun refreshAuthState(): Completable {
        return tokenStore.accessToken().doOnSuccess { token ->
            stateSubject.onNext(
                if (token.isNotEmpty()) AuthState.Authorized else AuthState.Unauthorized
            )
        }.ignoreElement()
    }

    fun startAuthorize(activity: Activity) {
        val state = randomState()
        tokenStore.storeOAuthState(state).blockingAwait()
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

    fun handleCallback(uri: Uri): Completable {
        val code = uri.getQueryParameter("code")
        val returnedState = uri.getQueryParameter("state")
        val error = uri.getQueryParameter("error")
        if (error != null) {
            stateSubject.onNext(AuthState.Error(error))
            return Completable.complete()
        }
        if (code.isNullOrEmpty() || returnedState.isNullOrEmpty()) {
            stateSubject.onNext(AuthState.Error("missing code or state"))
            return Completable.complete()
        }
        return tokenStore.consumeOAuthState().flatMapCompletable { stored ->
            if (stored.isEmpty() || stored != returnedState) {
                stateSubject.onNext(AuthState.Error("state mismatch"))
                Completable.complete()
            } else {
                exchangeCode(code)
            }
        }
    }

    private fun exchangeCode(code: String): Completable {
        val basic = Credentials.basic(BuildConfig.CLIENT_ID, BuildConfig.CLIENT_SECRET)
        return authApi.exchangeCode(
            basicAuth = basic,
            code = code,
            redirectUri = BuildConfig.REDIRECT_URI
        ).flatMapCompletable { resp ->
            val expiresAt = System.currentTimeMillis() / 1000L + resp.expiresIn
            tokenStore.saveTokens(resp.accessToken, resp.refreshToken, expiresAt)
                .doOnComplete { stateSubject.onNext(AuthState.Authorized) }
        }.doOnError { stateSubject.onNext(AuthState.Error(it.message ?: "exchange failed")) }
    }

    fun signOut(): Completable {
        return tokenStore.clearTokens()
            .doOnComplete { stateSubject.onNext(AuthState.Unauthorized) }
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
