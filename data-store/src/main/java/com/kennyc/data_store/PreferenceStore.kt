package com.kennyc.data_store

import android.content.Context
import com.kennyc.solarviewer.data.LocalSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class PreferenceStore(context: Context) : LocalSettings {


    override suspend fun store(key: String, value: String) {
        // TODO Convert to RX
    }

    override fun getStoredString(key: String, defaultValue: String): Flow<String> {
        // TODO Convert to RX
        return flowOf()
    }
}