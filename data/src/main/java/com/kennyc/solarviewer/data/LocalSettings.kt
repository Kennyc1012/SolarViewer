package com.kennyc.solarviewer.data

import kotlinx.coroutines.flow.Flow

interface LocalSettings {

    fun getStoredString(key: String, defaultValue: String): Flow<String>

    suspend fun store(key: String, value: String)
}