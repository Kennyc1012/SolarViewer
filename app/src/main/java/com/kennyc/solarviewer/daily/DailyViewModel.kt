package com.kennyc.solarviewer.daily

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kennyc.solarviewer.data.Clock
import com.kennyc.solarviewer.data.SolarRepository
import com.kennyc.solarviewer.data.model.ConsumptionStats
import com.kennyc.solarviewer.data.model.EMPTY_SYSTEM
import com.kennyc.solarviewer.data.model.ProductionStats
import com.kennyc.solarviewer.data.model.SolarGraphData
import com.kennyc.solarviewer.utils.ContentState
import com.kennyc.solarviewer.utils.ErrorState
import com.kennyc.solarviewer.utils.LoadingState
import com.kennyc.solarviewer.utils.UiState
import com.kennyc.solarviewer.utils.toNegative
// BarPoint lives in same package (daily)
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import java.util.Date
import java.util.concurrent.TimeUnit
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DailyViewModel @Inject constructor(
    private val repo: SolarRepository,
    private val clock: Clock
) : ViewModel() {

    private val _selectedBarPoint = MutableStateFlow(BarPoint.EMPTY_POINT)
    val selectedBarPoint: StateFlow<BarPoint> = _selectedBarPoint.asStateFlow()

    private val _state = MutableStateFlow<UiState>(LoadingState)
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var job: Job? = null

    private fun buildData(
        consumed: List<ConsumptionStats>,
        produced: List<ProductionStats>
    ): List<SolarGraphData> {
        val entries = mutableListOf<SolarGraphData>()
        for (x in consumed.indices) {
            val consumedItem = consumed[x]
            val producedItem = produced[x]
            val date = Date(TimeUnit.SECONDS.toMillis(producedItem.endingAtTS))
            entries.add(
                SolarGraphData(
                    x.toFloat(),
                    producedItem.powerCreatedInWh.toFloat(),
                    consumedItem.powerConsumedInWh.toNegative().toFloat(),
                    date
                )
            )
        }
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

    fun setSelectedBarPoint(point: BarPoint) {
        _selectedBarPoint.value = point
    }

    fun refresh() {
        job?.cancel()
        job = viewModelScope.launch {
            combine(
                repo.selectedDate(),
                repo.selectedSystem().filter { it != EMPTY_SYSTEM }
            ) { date, system -> date to system }
                .collectLatest { (date, system) ->
                    _state.value = LoadingState
                    val start = clock.midnight(date)
                    val end = (start + TimeUnit.HOURS.toMillis(24))
                        .takeIf { !clock.isInFuture(it) }
                    _state.value = try {
                        coroutineScope {
                            val results = listOf(
                                async { repo.getProductionStats(system, start, end) },
                                async { repo.getConsumptionStats(system, start, end) }
                            ).awaitAll()
                            @Suppress("UNCHECKED_CAST")
                            val production = results[0] as List<ProductionStats>
                            @Suppress("UNCHECKED_CAST")
                            val consumption = results[1] as List<ConsumptionStats>
                            ContentState(buildData(consumption, production))
                        }
                    } catch (t: Throwable) {
                        ErrorState(t)
                    }
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }
}

private const val DAY_STAT_SIZE = 96
