package com.kennyc.solarviewer.daily

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kennyc.solarviewer.data.Clock
import com.kennyc.solarviewer.data.SolarRepository
import com.kennyc.solarviewer.data.model.*
import com.kennyc.solarviewer.data.model.exception.RateLimitException
import com.kennyc.solarviewer.utils.MultiMediatorLiveData
import com.kennyc.solarviewer.utils.toNegative
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DailyViewModel @Inject constructor(
    private val repo: SolarRepository,
    private val clock: Clock,
    private val provider: CoroutineDispatchProvider
) : ViewModel() {

    val dateError = MutableLiveData<Unit>()

    val rateLimitError = MutableLiveData<Unit>()

    private val selectedDate = MutableLiveData<Date>()

    private val selectedSystem = MutableLiveData<SolarSystem>()

    val solarData: LiveData<List<SolarGraphData>> =
        MultiMediatorLiveData<List<SolarGraphData>>().apply {
            addSources(selectedDate, selectedSystem) { date, system ->
                viewModelScope.launch(provider.io) {
                    val startTime = clock.midnight(date)
                    val endDay = (startTime + TimeUnit.HOURS.toMillis(24))
                        .takeIf { !clock.isInFuture(it) }

                    try {
                        val production = repo.getProductionStats(system, startTime, endDay)
                        val consumption = repo.getConsumptionStats(system, startTime, endDay)
                        withContext(provider.main) { value = buildData(consumption, production) }
                    } catch (e: Exception) {
                        when (e) {
                            is RateLimitException -> withContext(provider.main) {
                                rateLimitError.value = Unit
                            }

                            else -> withContext(provider.main) { dateError.value = Unit }
                        }
                    }
                }
            }
        }

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

    fun setSelectedSystem(system: SolarSystem) {
        selectedSystem.value = system
    }

    fun setSelectedDate(date: Date) {
        selectedDate.value = date
    }
}

// The amount of items a full day of stats should contain
private const val DAY_STAT_SIZE = 96