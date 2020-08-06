package com.kennyc.solarviewer.data.model

import java.util.*

data class SolarSystemSummary(
    val systemId: String,
    val numModules: Int,
    val sizeInWatts: Int,
    val currentPowerInWatts: Int,
    val energyTodayInWatts: Int,
    val energyLifetimeInWatts: Int,
    val status: SystemStatus,
    val lastReported: Date
)