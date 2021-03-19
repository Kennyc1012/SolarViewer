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
import com.kennyc.solarviewer.data.model.*
import com.kennyc.solarviewer.data.model.exception.InvalidDateRangeException
import com.kennyc.solarviewer.data.model.exception.RateLimitException
import com.kennyc.solarviewer.data.rx.CompletableSubscriber
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

private const val TAG = "SolarRepository"

private const val KEY_SYSTEM_SUMMARY = ".SYSTEM_SUMMARY"
private const val KEY_SYSTEM_PRODUCTION = ".SYSTEM_PRODUCTION"
private const val KEY_SYSTEM_CONSUMPTION = ".SYSTEM_CONSUMPTION"

class EnphaseSolarRepository(
    context: Context,
    clock: Clock,
    private val api: EnphaseApi,
    private val logger: Logger
) : SolarRepository {

    private val cache = TimedCache(logger, clock)

    private val dao: EnphaseDao = Room.databaseBuilder(
        context,
        EnphaseDatabase::class.java,
        "solar_viewer.db"
    ).build().dao()

    override fun getSolarSystems(): Flowable<List<SolarSystem>> {
        // TODO force refresh of systems manually and based on time
        return dao.getSystems()
            .flatMap {
                if (it.isNullOrEmpty()) {
                    logger.v(TAG, "No items present, fetching from API")
                    fetchSystems()
                } else {
                    Flowable.just(it)
                }
            }
            .map { it.map { system -> SolarSystem(system.id, system.name, SystemStatus.NORMAL) } }
    }

    override fun getSystemSummary(solarSystem: SolarSystem): Single<SolarSystemSummary> {
        val key = solarSystem.id + KEY_SYSTEM_SUMMARY
        val cached = cache.get(key)
        if (cached is SolarSystemSummary) return Single.just(cached)

        return api.getSystemSummary(solarSystem.id)
            .map {
                SolarSystemSummary(
                    it.systemId,
                    it.numModules,
                    it.sizeInWatts,
                    it.currentPowerInWatts,
                    it.energyTodayInWatts,
                    it.energyLifetimeInWatts,
                    toSystemStatus(it.status),
                    Date(TimeUnit.SECONDS.toMillis(it.lastReportTS))
                )
            }
            .doOnSuccess { cache.put(key, it) }
            .onErrorResumeNext { Single.error(convertException(it)) }
    }

    override fun getProductionStats(
        solarSystem: SolarSystem,
        startTime: Long,
        endTime: Long?
    ): Single<List<ProductionStats>> {
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
            null ->
                api.getProductionStats(solarSystem.id, startTimeInSeconds, endTimeInSeconds)
                    .map { response ->
                        response.stats.map { ProductionStats(it.endingAtTS, it.powerCreatedInWh) }
                    }
                    .doOnSuccess { cache.put(key, it) }
                    .onErrorResumeNext { Single.error(convertException(it)) }

            else -> Single.just(list)
        }
    }

    override fun getConsumptionStats(
        solarSystem: SolarSystem,
        startTime: Long,
        endTime: Long?
    ): Single<List<ConsumptionStats>> {
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
            null ->
                api.getConsumptionStats(solarSystem.id, startTimeInSeconds, endTimeInSeconds)
                    .map { response ->
                        response.stats.map { ConsumptionStats(it.endingAtTS, it.powerConsumedInWh) }
                    }
                    .doOnSuccess { cache.put(key, it) }
                    .onErrorResumeNext { Single.error(convertException(it)) }

            else -> Single.just(list)
        }
    }

    override fun getSystemReport(
        solarSystem: SolarSystem,
        startTime: Long,
        endTime: Long?
    ): Single<SolarSystemReport> {
        return getConsumptionStats(solarSystem, startTime, endTime)
            .zipWith(getProductionStats(solarSystem, startTime, endTime),
                { consumption, production ->
                    require(consumption.size == production.size) { "Sizes are not the same" }

                    var totalImport = 0
                    var totalExport = 0
                    for (i in consumption.indices) {
                        val consumed = consumption[i]
                        val produced = production[i]

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

                    SolarSystemReport(
                        production.sumBy { it.powerCreatedInWh },
                        consumption.sumBy { it.powerConsumedInWh },
                        totalExport,
                        totalImport,
                        Date(TimeUnit.SECONDS.toMillis(consumption.last().endingAtTS))
                    )
                })
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

    private fun fetchSystems(): Flowable<List<RoomSolarSystem>> {
        return api.getSystems()
            .subscribeOn(Schedulers.io())
            .map { response ->
                val apiSystems = response.systems
                    .map { SolarSystem(it.id, it.name, toSystemStatus(it.status)) }

                val currentTime = System.currentTimeMillis()
                apiSystems.map { RoomSolarSystem(it.id, it.name, currentTime, it.status) }
            }.toFlowable()
            .doOnNext {
                dao.insertSystems(it)
                    .subscribeOn(Schedulers.io())
                    .subscribe(CompletableSubscriber.stub())
            }
    }
}