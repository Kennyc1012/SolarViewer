package com.kennyc.solarviewer.data

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single


interface LocalSettings {

    fun getStoredString(key: String, defaultValue: String): Single<String>

    fun store(key: String, value: String): Completable
}