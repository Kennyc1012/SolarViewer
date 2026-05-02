package com.kennyc.data_enphase

import android.content.Context
import androidx.room.Room
import com.kennyc.api_enphase.EnphaseApi
import com.kennyc.api_enphase.model.exception.NetworkException
import com.kennyc.data_enphase.cache.TimedCache
import com.kennyc.data_enphase.db.EnphaseDao
import com.kennyc.data_enphase.db.EnphaseDatabase
import com.kennyc.data_enphase.db.model.RoomSolarSystem
import com.kennyc.solarviewer.data.Clock
import com.kennyc.solarviewer.data.Logger
import com.kennyc.solarviewer.data.SolarRepository
import com.kennyc.solarviewer.data.model.ConsumptionStats
import com.kennyc.solarviewer.data.model.EMPTY_SYSTEM
import com.kennyc.solarviewer.data.model.ProductionStats
import com.kennyc.solarviewer.data.model.SolarSystem
import com.kennyc.solarviewer.data.model.SolarSystemReport
import com.kennyc.solarviewer.data.model.SolarSystemSummary
import com.kennyc.solarviewer.data.model.SystemStatus
import com.kennyc.solarviewer.data.model.exception.InvalidDateRangeException
import com.kennyc.solarviewer.data.model.exception.RateLimitException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.math.abs

private const val TAG = "SolarRepository"

private const val KEY_SYSTEM_SUMMARY = ".SYSTEM_SUMMARY"
private const val KEY_SYSTEM_PRODUCTION = ".SYSTEM_PRODUCTION"
private const val KEY_SYSTEM_CONSUMPTION = ".SYSTEM_CONSUMPTION"

class EnphaseSolarRepository(
    context: Context,
    private val clock: Clock,
    private val api: EnphaseApi,
    private val logger: Logger
) : SolarRepository {

    private val cache = TimedCache(logger, clock)

    private val dao: EnphaseDao = Room.databaseBuilder(
        context,
        EnphaseDatabase::class.java,
        "solar_viewer.db"
    ).build().dao()

    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val systemFlow = MutableStateFlow<SolarSystem>(EMPTY_SYSTEM)
    private val dateFlow = MutableStateFlow<Date>(clock.currentDate())

    override var currentSystem: SolarSystem? = null
        set(value) {
            field = value
            systemFlow.value = value ?: EMPTY_SYSTEM
        }

    override var currentDate: Date? = null
        set(value) {
            field = value
            dateFlow.value = value ?: clock.currentDate()
        }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override fun getSolarSystems() = dao.getSystems()
        .flatMapLatest { rooms ->
            if (rooms.isEmpty()) {
                logger.v(TAG, "No items present, fetching from API")
                flow { emit(fetchAndStoreSystems()) }.catch { emit(emptyList()) }
            } else {
                flow { emit(rooms) }
            }
        }
        .map { rooms -> rooms.map { SolarSystem(it.id, it.name, SystemStatus.NORMAL) } }

    private suspend fun fetchAndStoreSystems(): List<RoomSolarSystem> {
        return try {
            val response = api.getSystems()
            val now = System.currentTimeMillis()
            val rooms = response.systems
                .map { SolarSystem(it.id, it.name, toSystemStatus(it.status)) }
                .map { RoomSolarSystem(it.id, it.name, now, it.status) }
            repoScope.launch { dao.insertSystems(rooms) }
            rooms
        } catch (t: Throwable) {
            throw convertException(t)
        }
    }

    override suspend fun getSystemSummary(solarSystem: SolarSystem): SolarSystemSummary {
        val key = solarSystem.id + KEY_SYSTEM_SUMMARY
        val cached = cache.get(key)
        if (cached is SolarSystemSummary) return cached

        return try {
            val it = api.getSystemSummary(solarSystem.id)
            SolarSystemSummary(
                it.systemId,
                it.numModules,
                it.sizeInWatts,
                it.currentPowerInWatts,
                it.energyTodayInWatts,
                it.energyLifetimeInWatts,
                toSystemStatus(it.status),
                Date(TimeUnit.SECONDS.toMillis(it.lastReportTS))
            ).also { cache.put(key, it) }
        } catch (t: Throwable) {
            throw convertException(t)
        }
    }

    override suspend fun getProductionStats(
        solarSystem: SolarSystem,
        startTime: Long,
        endTime: Long?
    ): List<ProductionStats> {
        val startTimeInSeconds = TimeUnit.MILLISECONDS.toSeconds(startTime)
        val endTimeInSeconds = endTime?.let { TimeUnit.MILLISECONDS.toSeconds(it) }

        val key =
            solarSystem.id + KEY_SYSTEM_PRODUCTION + ".START_$startTimeInSeconds.END_$endTimeInSeconds"
        val cached = cache.get(key)
        if (cached is List<*>) {
            val list = cached.filterIsInstance<ProductionStats>()
                .takeIf { it.isNotEmpty() && it.size == cached.size }
            if (list != null) return list
        }

        return try {
            val response = api.getProductionStats(solarSystem.id, startTimeInSeconds, endTimeInSeconds)
            response.stats.map { ProductionStats(it.endingAtTS, it.powerCreatedInWh) }
                .also { cache.put(key, it) }
        } catch (t: Throwable) {
            throw convertException(t)
        }
    }

    override suspend fun getConsumptionStats(
        solarSystem: SolarSystem,
        startTime: Long,
        endTime: Long?
    ): List<ConsumptionStats> {
        val startTimeInSeconds = TimeUnit.MILLISECONDS.toSeconds(startTime)
        val endTimeInSeconds = endTime?.let { TimeUnit.MILLISECONDS.toSeconds(it) }

        val key =
            solarSystem.id + KEY_SYSTEM_CONSUMPTION + ".START_$startTimeInSeconds.END_$endTimeInSeconds"
        val cached = cache.get(key)
        if (cached is List<*>) {
            val list = cached.filterIsInstance<ConsumptionStats>()
                .takeIf { it.isNotEmpty() && it.size == cached.size }
            if (list != null) return list
        }

        return try {
            val response = api.getConsumptionStats(solarSystem.id, startTimeInSeconds, endTimeInSeconds)
            response.stats.map { ConsumptionStats(it.endingAtTS, it.powerConsumedInWh) }
                .also { cache.put(key, it) }
        } catch (t: Throwable) {
            throw convertException(t)
        }
    }

    override suspend fun getSystemReport(
        solarSystem: SolarSystem,
        startTime: Long,
        endTime: Long?
    ): SolarSystemReport = coroutineScope {
        val (consumption, production) = listOf(
            async { getConsumptionStats(solarSystem, startTime, endTime) },
            async { getProductionStats(solarSystem, startTime, endTime) }
        ).awaitAll().let { it[0] as List<ConsumptionStats> to it[1] as List<ProductionStats> }

        require(consumption.size == production.size) { "Sizes are not the same" }

        var totalImport = 0
        var totalExport = 0
        for (i in consumption.indices) {
            val consumed = consumption[i]
            val produced = production[i]
            if (consumed.endingAtTS != produced.endingAtTS) {
                logger.w(
                    TAG,
                    "Items at index $i did not have the same time. consumed: ${consumed.endingAtTS}, produced: ${produced.endingAtTS}"
                )
                break
            }
            val net = produced.powerCreatedInWh - consumed.powerConsumedInWh
            if (net > 0) totalExport += net else totalImport += abs(net)
        }

        SolarSystemReport(
            production.sumOf { it.powerCreatedInWh },
            consumption.sumOf { it.powerConsumedInWh },
            totalExport,
            totalImport,
            Date(TimeUnit.SECONDS.toMillis(consumption.last().endingAtTS))
        )
    }

    override fun selectedSystem(): StateFlow<SolarSystem> = systemFlow.asStateFlow()

    override fun selectedDate(): StateFlow<Date> = dateFlow.asStateFlow()

    private fun toSystemStatus(value: String): SystemStatus = when (value) {
        "comm" -> SystemStatus.COMMUNICATION_ERROR
        "power" -> SystemStatus.PRODUCTION_ERROR
        "meter", "meter_issue" -> SystemStatus.METER_ERROR
        "micro" -> SystemStatus.MICROINVERTER_ERROR
        "battery" -> SystemStatus.BATTERY_ERROR
        "normal" -> SystemStatus.NORMAL
        else -> SystemStatus.UNKNOWN
    }

    private fun convertException(exception: Throwable): Throwable {
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
