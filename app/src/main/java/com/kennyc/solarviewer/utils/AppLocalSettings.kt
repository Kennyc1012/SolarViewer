package com.kennyc.solarviewer.utils

import android.content.Context
import androidx.preference.PreferenceManager
import com.kennyc.solarviewer.data.LocalSettings

class AppLocalSettings(context: Context) : LocalSettings {

    private val pref = PreferenceManager.getDefaultSharedPreferences(context)

    override fun store(key: String, value: String?) = pref.edit().putString(key, value).apply()

    override fun getStoredString(key: String, defaultValue: String?): String? =
        pref.getString(key, defaultValue)
}