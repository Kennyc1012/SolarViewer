package com.kennyc.solarviewer.data

import com.kennyc.solarviewer.data.model.*

interface SolarRepository {

    suspend fun getSolarSystems(): List<SolarSystem>

    suspend fun getSystemSummary(solarSystem: SolarSystem): SolarSystemSummary

    suspend fun getProductionStats(
        solarSystem: SolarSystem,
        startTime: Long,
        endTime: Long?
    ): List<ProductionStats>

    suspend fun getConsumptionStats(
        solarSystem: SolarSystem,
        startTime: Long,
        endTime: Long?
    ): List<ConsumptionStats>

    suspend fun getSystemReport(
        solarSystem: SolarSystem,
        startTime: Long,
        endTime: Long?
    ): SolarSystemReport
}