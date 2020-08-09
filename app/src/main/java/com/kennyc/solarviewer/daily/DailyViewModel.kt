package com.kennyc.solarviewer.daily

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.kennyc.solarviewer.R
import com.kennyc.solarviewer.data.Clock
import com.kennyc.solarviewer.data.SolarRepository
import com.kennyc.solarviewer.data.model.ConsumptionStats
import com.kennyc.solarviewer.data.model.CoroutineDispatchProvider
import com.kennyc.solarviewer.data.model.ProductionStats
import com.kennyc.solarviewer.data.model.SolarSystem
import com.kennyc.solarviewer.data.model.exception.RateLimitException
import com.kennyc.solarviewer.utils.asKilowattString
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
    private val context: Context,
    private val provider: CoroutineDispatchProvider
) : ViewModel() {

    val dateError = MutableLiveData<Unit>()

    val rateLimitError = MutableLiveData<Unit>()
    private val selectedDate = BroadcastChannel<Date>(1)

    private val selectedSystem = BroadcastChannel<SolarSystem>(1)

    val lineData = selectedDate.asFlow()
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
    ): BarData {
        val entries = mutableListOf<BarEntry>()

        for (x in consumed.indices) {
            val consumedItem = consumed[x]
            val producedItem = produced[x]
            val date = Date(TimeUnit.SECONDS.toMillis(producedItem.endingAtTS))

            val entry = BarEntry(
                x.toFloat(),
                floatArrayOf(
                    producedItem.powerCreatedInWh.toFloat(),
                    consumedItem.powerConsumedInWh.toNegative().toFloat()
                ),
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
                entries.add(BarEntry(x.toFloat(), floatArrayOf(0f, 0f), date))
            }
        }

        val barColors = Array(entries.size) {
            when (it % 2 == 0) {
                true -> context.resources.getColor(R.color.color_production, context.theme)
                else -> context.resources.getColor(R.color.color_consumption, context.theme)
            }
        }.toList()

        val dataSet = BarDataSet(entries, null).apply {
            val consumedLabel = context.getString(
                R.string.daily_consumed,
                consumed.sumBy { it.powerConsumedInWh }.asKilowattString()
            )

            val producedLabel = context.getString(
                R.string.daily_produced,
                produced.sumBy { it.powerCreatedInWh }.asKilowattString()
            )

            stackLabels = arrayOf(producedLabel, consumedLabel)
            colors = barColors
            setDrawValues(false)
        }

        return BarData(dataSet)
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