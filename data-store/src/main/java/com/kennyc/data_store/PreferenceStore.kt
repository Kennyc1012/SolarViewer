package com.kennyc.data_store

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder
import com.kennyc.solarviewer.data.LocalSettings
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class PreferenceStore(context: Context) : LocalSettings {

    private val store = RxPreferenceDataStoreBuilder(context, "PreferenceStore")
        .setIoScheduler(Schedulers.io())
        .build()


    override fun store(key: String, value: String): Completable {
        return store.updateDataAsync {
            val prefs = it.toMutablePreferences()
            prefs[stringPreferencesKey(key)] = value
            Single.just(prefs)
        }.flatMapCompletable { Completable.complete() }
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
}