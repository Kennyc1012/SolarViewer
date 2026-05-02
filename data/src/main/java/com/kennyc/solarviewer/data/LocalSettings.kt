package com.kennyc.solarviewer.data

interface LocalSettings {

    suspend fun getStoredString(key: String, defaultValue: String): String

    suspend fun store(key: String, value: String)
}
