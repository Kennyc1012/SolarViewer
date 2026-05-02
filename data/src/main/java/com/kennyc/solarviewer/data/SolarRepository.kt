package com.kennyc.solarviewer.data

import com.kennyc.solarviewer.data.model.ConsumptionStats
import com.kennyc.solarviewer.data.model.ProductionStats
import com.kennyc.solarviewer.data.model.SolarSystem
import com.kennyc.solarviewer.data.model.SolarSystemReport
import com.kennyc.solarviewer.data.model.SolarSystemSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.util.Date

interface SolarRepository {

    fun getSolarSystems(): Flow<List<SolarSystem>>

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

    fun selectedSystem(): StateFlow<SolarSystem>

    fun selectedDate(): StateFlow<Date>

    var currentSystem: SolarSystem?

    var currentDate: Date?
}
