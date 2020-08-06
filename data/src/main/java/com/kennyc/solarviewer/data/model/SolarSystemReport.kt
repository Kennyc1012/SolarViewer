package com.kennyc.solarviewer.data.model

import java.util.*

data class SolarSystemReport(
    // Total energy produce
    val productionInWatts: Int,
    // Total energy consumed, both from grid and solar
    val consumptionInWatts: Int,
    // Total energy exported
    val exportedInWatts: Int,
    // Total energy imported from grid
    val importedInWatts: Int,
    // Last time when the data was updated
    val lastReported: Date
) {
    val netEnergy: Int = productionInWatts - consumptionInWatts

    val isNetPositive: Boolean = netEnergy > 0
}