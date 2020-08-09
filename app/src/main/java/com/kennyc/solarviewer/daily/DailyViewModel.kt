package com.kennyc.solarviewer.daily

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.kennyc.solarviewer.data.Clock
import com.kennyc.solarviewer.data.SolarRepository
import com.kennyc.solarviewer.data.model.*
import com.kennyc.solarviewer.data.model.exception.RateLimitException
import com.kennyc.solarviewer.utils.toNegative
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@ExperimentalCoroutinesApi
@FlowPreview
class DailyViewModel @Inject constructor(
    private val repo: SolarRepository,
    private val clock: Clock,
    private val provider: CoroutineDispatchProvider
) : ViewModel() {

    val dateError = MutableLiveData<Unit>()

    val rateLimitError = MutableLiveData<Unit>()
    private val selectedDate = BroadcastChannel<Date>(1)

    private val selectedSystem = BroadcastChannel<SolarSystem>(1)

    val solarData = selectedDate.asFlow()
        .combine(selectedSystem.asFlow()) { date, system ->
            val startTime = clock.midnight(date)
            val endDay = (startTime + TimeUnit.HOURS.toMillis(24))
                .takeIf { !clock.isInFuture(it) }

            val production = repo.getProductionStats(system, startTime, endDay)
            val consumption = repo.getConsumptionStats(system, startTime, endDay)

            buildData(consumption, production)
        }.catch {
            when (it) {
                is RateLimitException -> withContext(provider.main) { rateLimitError.value = Unit }

                else -> withContext(provider.main) { dateError.value = Unit }
            }
        }
        .asLiveData(viewModelScope.coroutineContext, Duration.ofSeconds(10))

    private fun buildData(
        consumed: List<ConsumptionStats>,
        produced: List<ProductionStats>
    ): List<SolarGraphData> {
        val entries = mutableListOf<SolarGraphData>()

        for (x in consumed.indices) {
            val consumedItem = consumed[x]
            val producedItem = produced[x]
            val date = Date(TimeUnit.SECONDS.toMillis(producedItem.endingAtTS))

            val entry = SolarGraphData(
                x.toFloat(),
                producedItem.powerCreatedInWh.toFloat(),
                consumedItem.powerConsumedInWh.toNegative().toFloat(),
                date
            )

            entries.add(entry)
        }

        // We do not a full days worth of data, add "empty" objects until we are full to show a full graph
        if (entries.size < DAY_STAT_SIZE) {
            var time = consumed.last().endingAtTS

            for (x in entries.size until DAY_STAT_SIZE) {
                time += TimeUnit.MINUTES.toSeconds(15)
                val date = Date(TimeUnit.SECONDS.toMillis(time))
                entries.add(SolarGraphData(x.toFloat(), 0f, 0f, date))
            }
        }

        return entries
    }

    fun setSelectedSystem(system: SolarSystem) = selectedSystem.offer(system)

    fun setSelectedDate(date: Date) {
        // TODO There is probably a better way to do this by limiting the dates in the picker
        if (clock.isInFuture(date)) {
            dateError.value = Unit
            return
        }

        selectedDate.offer(date)
    }
}

// The amount of items a full day of stats should contain
private const val DAY_STAT_SIZE = 96