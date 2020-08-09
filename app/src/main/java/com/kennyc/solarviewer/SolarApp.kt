package com.kennyc.solarviewer

import android.app.Application
import android.os.StrictMode
import com.kennyc.solarviewer.data.Logger
import com.kennyc.solarviewer.di.components.DaggerAppComponent

class SolarApp : Application() {

    val component by lazy {
        DaggerAppComponent
            .builder()
            .appContext(applicationContext)
            .isDebug(BuildConfig.DEBUG)
            .build()
    }

    lateinit var logger: Logger

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
        }

        logger = component.logger()
    }
}