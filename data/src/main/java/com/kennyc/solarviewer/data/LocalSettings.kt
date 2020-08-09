package com.kennyc.solarviewer.data

interface LocalSettings {

    fun store(key: String, value: String?)

    fun getStoredString(key: String, defaultValue: String? = null): String?

}