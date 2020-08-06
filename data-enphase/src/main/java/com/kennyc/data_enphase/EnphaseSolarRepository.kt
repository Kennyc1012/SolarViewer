package com.kennyc.data_enphase

import android.content.Context
import androidx.room.Room
import com.kennyc.api_enphase.EnphaseApi
import com.kennyc.api_enphase.model.exception.NetworkException
import com.kennyc.data_enphase.db.EnphaseDao
import com.kennyc.data_enphase.db.EnphaseDatabase
import com.kennyc.data_enphase.db.model.RoomSolarSystem
import com.kennyc.solarviewer.data.Logger
import com.kennyc.solarviewer.data.SolarRepository
import com.kennyc.solarviewer.data.cache.TimedCache
import com.kennyc.solarviewer.data.model.*
import com.kennyc.solarviewer.data.model.exception.InvalidDateRangeException
import com.kennyc.solarviewer.data.model.exception.RateLimitException
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

private const val TAG = "SolarRepository"

private const val KEY_SYSTEM_SUMMARY = ".SYSTEM_SUMMARY"
private const val KEY_SYSTEM_PRODUCTION = ".SYSTEM_PRODUCTION"
private const val KEY_SYSTEM_CONSUMPTION = ".SYSTEM_CONSUMPTION"

class EnphaseSolarRepository(
    context: Context,
    private val api: EnphaseApi,
    private val logger: Logger,
    private val cache: TimedCache
) : SolarRepository {

    private val dao: EnphaseDao = Room.databaseBuilder(
        context,
        EnphaseDatabase::class.java,
        "solar_viewer.db"
    ).build().dao()

    override suspend fun getSolarSystems(): List<SolarSystem> {
        // TODO force refresh of systems manually and based on time
        val systems = dao.getSystems().map { SolarSystem(it.id, it.name, SystemStatus.NORMAL) }
        // Check if our cached system had a bad status, refresh if it did
        val badSystem = systems.firstOrNull { it.status != SystemStatus.NORMAL }

        return if (systems.isNotEmpty() && badSystem == null) {
            logger.v(TAG, "Systems current present in database")
            systems
        } else {
            logger.v(TAG, "Systems current not present in database, fetching...")

            try {
                val apiSystems = api.getSystems().systems
                    .map { SolarSystem(it.id, it.name, toSystemStatus(it.status)) }


                val currentTime = System.currentTimeMillis()
                val toInsert =
                    apiSystems.map { RoomSolarSystem(it.id, it.name, currentTime, it.status) }
                dao.insertSystems(toInsert)
                apiSystems
            } catch (e: Exception) {
                throw convertException(e)
            }
        }
    }

    override suspend fun getSystemSummary(solarSystem: SolarSystem): SolarSystemSummary {
        val key = solarSystem.id + KEY_SYSTEM_SUMMARY
        return when (val cached = cache.get(key)) {
            is SolarSystemSummary -> cached

            else -> {
                try {
                    api.getSystemSummary(solarSystem.id)
                        .run {
                            SolarSystemSummary(
                                systemId,
                                numModules,
                                sizeInWatts,
                                currentPowerInWatts,
                                energyTodayInWatts,
                                energyLifetimeInWatts,
                                toSystemStatus(status),
                                Date(TimeUnit.SECONDS.toMillis(lastReportTS))
                            ).apply { cache.put(key, this) }
                        }
                } catch (e: Exception) {
                    throw convertException(e)
                }
            }
        }

    }

    override suspend fun getProductionStats(
        solarSystem: SolarSystem,
        startTime: Long,
        endTime: Long?
    ): List<ProductionStats> {
        val startTimeInSeconds = TimeUnit.MILLISECONDS.toSeconds(startTime)

        val endTimeInSeconds = when (endTime) {
            null -> null
            else -> TimeUnit.MILLISECONDS.toSeconds(endTime)
        }

        val key =
            solarSystem.id + KEY_SYSTEM_PRODUCTION + ".START_$startTimeInSeconds.END_$endTimeInSeconds"
        val cached = cache.get(key)
        var list: List<ProductionStats>? = null

        if (cached is List<*>) {
            list = cached.filterIsInstance(ProductionStats::class.java)
                .takeIf { it.isNotEmpty() && it.size == cached.size }
        }

        return when (list) {
            null -> try {
                api.getProductionStats(solarSystem.id, startTimeInSeconds, endTimeInSeconds)
                    .stats
                    .map { ProductionStats(it.endingAtTS, it.powerCreatedInWh) }
                    .apply { cache.put(key, this) }
            } catch (e: Exception) {
                throw convertException(e)
            }

            else -> list
        }
    }

    override suspend fun getConsumptionStats(
        solarSystem: SolarSystem,
        startTime: Long,
        endTime: Long?
    ): List<ConsumptionStats> {
        val startTimeInSeconds = TimeUnit.MILLISECONDS.toSeconds(startTime)

        val endTimeInSeconds = when (endTime) {
            null -> null
            else -> TimeUnit.MILLISECONDS.toSeconds(endTime)
        }

        val key =
            solarSystem.id + KEY_SYSTEM_CONSUMPTION + ".START_$startTimeInSeconds.END_$endTimeInSeconds"
        val cached = cache.get(key)
        var list: List<ConsumptionStats>? = null

        if (cached is List<*>) {
            list = cached.filterIsInstance(ConsumptionStats::class.java)
                .takeIf { it.isNotEmpty() && it.size == cached.size }
        }

        return when (list) {
            null -> try {
                api.getConsumptionStats(solarSystem.id, startTimeInSeconds, endTimeInSeconds)
                    .stats
                    .map { ConsumptionStats(it.endingAtTS, it.powerConsumedInWh) }
                    .apply {
                        cache.put(key, this)
                    }
            } catch (e: Exception) {
                throw convertException(e)
            }

            else -> list
        }
    }

    override suspend fun getSystemReport(
        solarSystem: SolarSystem,
        startTime: Long,
        endTime: Long?
    ): SolarSystemReport {
        val consumptionStats = getConsumptionStats(solarSystem, startTime, endTime)
        val produce = getProductionStats(solarSystem, startTime, endTime)

        require(consumptionStats.size == produce.size) { "Sizes are not the same" }

        var totalImport = 0
        var totalExport = 0
        for (i in consumptionStats.indices) {
            val consumed = consumptionStats[i]
            val produced = produce[i]

            // Each item should have the same end time
            if (consumed.endingAtTS != produced.endingAtTS) {
                logger.w(
                    TAG,
                    "Items at index $i did not have the same time. consumed: ${consumed.endingAtTS}, produced: ${produced.endingAtTS}"
                )
                break
            }

            val net = produced.powerCreatedInWh - consumed.powerConsumedInWh
            if (net > 0) {
                totalExport += net
            } else {
                totalImport += abs(net)
            }
        }

        return SolarSystemReport(
            produce.sumBy { it.powerCreatedInWh },
            consumptionStats.sumBy { it.powerConsumedInWh },
            totalExport,
            totalImport,
            Date(TimeUnit.SECONDS.toMillis(consumptionStats.last().endingAtTS))
        )
    }

    private fun toSystemStatus(value: String): SystemStatus {
        return when (value) {
            "comm" -> SystemStatus.COMMUNICATION_ERROR
            "power" -> SystemStatus.PRODUCTION_ERROR
            "meter", "meter_issue" -> SystemStatus.METER_ERROR
            "micro" -> SystemStatus.MICROINVERTER_ERROR
            "battery" -> SystemStatus.BATTERY_ERROR
            "normal" -> SystemStatus.NORMAL
            else -> SystemStatus.UNKNOWN
        }
    }

    private fun convertException(exception: Exception): Exception {
        if (exception is NetworkException) {
            return when (exception.code) {
                409 -> RateLimitException()
                422 -> InvalidDateRangeException()
                else -> exception
            }
        }

        return exception
    }
}