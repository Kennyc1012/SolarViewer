package com.kennyc.data_store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kennyc.api_enphase.auth.TokenStore
import com.kennyc.solarviewer.data.LocalSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicReference

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("PreferenceStore")

class PreferenceStore(context: Context) : LocalSettings, TokenStore {

    private val store: DataStore<Preferences> = context.applicationContext.dataStore

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private data class Snapshot(val accessToken: String, val refreshToken: String)

    private val tokenSnapshot = AtomicReference(Snapshot("", ""))

    init {
        // Prime the cache synchronously so the first network request has a token.
        runBlocking {
            val prefs = store.data.first()
            tokenSnapshot.set(
                Snapshot(
                    prefs[KEY_ACCESS_TOKEN].orEmpty(),
                    prefs[KEY_REFRESH_TOKEN].orEmpty()
                )
            )
        }
        store.data.onEach { prefs ->
            tokenSnapshot.set(
                Snapshot(
                    prefs[KEY_ACCESS_TOKEN].orEmpty(),
                    prefs[KEY_REFRESH_TOKEN].orEmpty()
                )
            )
        }.launchIn(scope)
    }

    override suspend fun store(key: String, value: String) {
        store.edit { it[stringPreferencesKey(key)] = value }
    }

    override suspend fun getStoredString(key: String, defaultValue: String): String {
        return store.data.map { it[stringPreferencesKey(key)] ?: defaultValue }.first()
    }

    override suspend fun saveTokens(
        accessToken: String,
        refreshToken: String,
        expiresAtEpochSec: Long
    ) {
        store.edit {
            it[KEY_ACCESS_TOKEN] = accessToken
            it[KEY_REFRESH_TOKEN] = refreshToken
            it[KEY_EXPIRES_AT] = expiresAtEpochSec
        }
        tokenSnapshot.set(Snapshot(accessToken, refreshToken))
    }

    override suspend fun clearTokens() {
        store.edit {
            it.remove(KEY_ACCESS_TOKEN)
            it.remove(KEY_REFRESH_TOKEN)
            it.remove(KEY_EXPIRES_AT)
            it.remove(KEY_OAUTH_STATE)
        }
        tokenSnapshot.set(Snapshot("", ""))
    }

    override fun accessTokenBlocking(): String? =
        tokenSnapshot.get().accessToken.takeIf { it.isNotEmpty() }

    override fun refreshTokenBlocking(): String? =
        tokenSnapshot.get().refreshToken.takeIf { it.isNotEmpty() }

    override fun accessToken(): Flow<String> =
        store.data.map { it[KEY_ACCESS_TOKEN].orEmpty() }

    override fun refreshToken(): Flow<String> =
        store.data.map { it[KEY_REFRESH_TOKEN].orEmpty() }

    override fun expiresAtEpochSec(): Flow<Long> =
        store.data.map { it[KEY_EXPIRES_AT] ?: 0L }

    override suspend fun storeOAuthState(state: String) {
        store.edit { it[KEY_OAUTH_STATE] = state }
    }

    override suspend fun consumeOAuthState(): String {
        val current = store.data.map { it[KEY_OAUTH_STATE].orEmpty() }.first()
        store.edit { it.remove(KEY_OAUTH_STATE) }
        return current
    }

    private companion object {
        val KEY_ACCESS_TOKEN = stringPreferencesKey("oauth_access_token")
        val KEY_REFRESH_TOKEN = stringPreferencesKey("oauth_refresh_token")
        val KEY_EXPIRES_AT = longPreferencesKey("oauth_expires_at")
        val KEY_OAUTH_STATE = stringPreferencesKey("oauth_state")
    }
}
