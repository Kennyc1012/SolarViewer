package com.kennyc.data_store

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.createDataStore
import com.kennyc.solarviewer.data.LocalSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class PreferenceStore(context: Context) : LocalSettings {

    private val store = context.createDataStore("PreferenceStore")

    override suspend fun store(key: String, value: String) {
        store.edit { it[preferencesKey(key)] = value }
    }

    override fun getStoredString(key: String, defaultValue: String): Flow<String> {
        return store.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { it[preferencesKey<String>(key)] ?: defaultValue }
    }
}