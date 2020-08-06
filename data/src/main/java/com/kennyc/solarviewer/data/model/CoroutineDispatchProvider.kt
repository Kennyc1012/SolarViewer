package com.kennyc.solarviewer.data.model

import kotlin.coroutines.CoroutineContext

interface CoroutineDispatchProvider {
    val main: CoroutineContext
    val default: CoroutineContext
    val io: CoroutineContext
    val unconfined: CoroutineContext
}