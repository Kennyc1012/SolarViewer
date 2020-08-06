package com.kennyc.solarviewer.utils

import android.util.Log
import com.kennyc.solarviewer.data.Logger

class AppLogger(private val isDebug: Boolean) : Logger {
    override fun v(tag: String, message: String) {
        if (isDebug) Log.v(tag, message)
    }

    override fun w(tag: String, message: String) {
        if (isDebug) Log.w(tag, message)
    }

    override fun i(tag: String, message: String) {
        if (isDebug) Log.i(tag, message)
    }

    override fun d(tag: String, message: String) {
        if (isDebug) Log.d(tag, message)
    }

    override fun e(tag: String, message: String) {
        if (isDebug) e(tag, message, null)
    }

    override fun e(tag: String, message: String, error: Throwable?) {
        if (isDebug) Log.e(tag, message, error)
    }
}