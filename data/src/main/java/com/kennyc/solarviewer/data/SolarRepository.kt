package com.kennyc.solarviewer.data

import com.kennyc.solarviewer.data.model.*
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single

interface SolarRepository {

    fun getSolarSystems(): Flowable<List<SolarSystem>>

    fun getSystemSummary(solarSystem: SolarSystem): Single<SolarSystemSummary>

    fun getProductionStats(
        solarSystem: SolarSystem,
        startTime: Long,
        endTime: Long?
    ): Single<List<ProductionStats>>

    fun getConsumptionStats(
        solarSystem: SolarSystem,
        startTime: Long,
        endTime: Long?
    ): Single<List<ConsumptionStats>>

    fun getSystemReport(
        solarSystem: SolarSystem,
        startTime: Long,
        endTime: Long?
    ): Single<SolarSystemReport>
}