package com.kennyc.data_store

import android.content.Context
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder
import com.kennyc.api_enphase.auth.TokenStore
import com.kennyc.solarviewer.data.LocalSettings
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class PreferenceStore(context: Context) : LocalSettings, TokenStore {

    private val store = RxPreferenceDataStoreBuilder(context, "PreferenceStore")
        .setIoScheduler(Schedulers.io())
        .build()

    override fun store(key: String, value: String): Completable {
        return store.updateDataAsync {
            val prefs = it.toMutablePreferences()
            prefs[stringPreferencesKey(key)] = value
            Single.just(prefs)
        }.ignoreElement()
            .onErrorComplete()
    }

    override fun getStoredString(key: String, defaultValue: String): Single<String> {
        return store.data().map {
            when (val saved = it[stringPreferencesKey(key)]) {
                null -> defaultValue
                else -> saved
            }
        }.first(defaultValue)
    }

    override fun saveTokens(
        accessToken: String,
        refreshToken: String,
        expiresAtEpochSec: Long
    ): Completable {
        return store.updateDataAsync {
            val prefs = it.toMutablePreferences()
            prefs[KEY_ACCESS_TOKEN] = accessToken
            prefs[KEY_REFRESH_TOKEN] = refreshToken
            prefs[KEY_EXPIRES_AT] = expiresAtEpochSec
            Single.just(prefs)
        }.ignoreElement()
    }

    override fun clearTokens(): Completable {
        return store.updateDataAsync {
            val prefs = it.toMutablePreferences()
            prefs.remove(KEY_ACCESS_TOKEN)
            prefs.remove(KEY_REFRESH_TOKEN)
            prefs.remove(KEY_EXPIRES_AT)
            prefs.remove(KEY_OAUTH_STATE)
            Single.just(prefs)
        }.ignoreElement()
    }

    override fun accessTokenBlocking(): String? =
        store.data().map { it[KEY_ACCESS_TOKEN] ?: "" }.blockingFirst().takeIf { it.isNotEmpty() }

    override fun refreshTokenBlocking(): String? =
        store.data().map { it[KEY_REFRESH_TOKEN] ?: "" }.blockingFirst().takeIf { it.isNotEmpty() }

    override fun accessToken(): Single<String> =
        store.data().map { it[KEY_ACCESS_TOKEN] ?: "" }.first("")

    override fun refreshToken(): Single<String> =
        store.data().map { it[KEY_REFRESH_TOKEN] ?: "" }.first("")

    override fun expiresAtEpochSec(): Single<Long> =
        store.data().map { it[KEY_EXPIRES_AT] ?: 0L }.first(0L)

    override fun storeOAuthState(state: String): Completable {
        return store.updateDataAsync {
            val prefs = it.toMutablePreferences()
            prefs[KEY_OAUTH_STATE] = state
            Single.just(prefs)
        }.ignoreElement()
    }

    override fun consumeOAuthState(): Single<String> {
        return store.data().map { it[KEY_OAUTH_STATE] ?: "" }.first("")
            .flatMap { current ->
                store.updateDataAsync {
                    val prefs = it.toMutablePreferences()
                    prefs.remove(KEY_OAUTH_STATE)
                    Single.just(prefs)
                }.map { current }
            }
            .onErrorReturnItem("")
    }

    private companion object {
        val KEY_ACCESS_TOKEN = stringPreferencesKey("oauth_access_token")
        val KEY_REFRESH_TOKEN = stringPreferencesKey("oauth_refresh_token")
        val KEY_EXPIRES_AT = longPreferencesKey("oauth_expires_at")
        val KEY_OAUTH_STATE = stringPreferencesKey("oauth_state")
    }
}
