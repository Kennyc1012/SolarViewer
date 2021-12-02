package com.kennyc.solarviewer.daily

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kennyc.solarviewer.data.Clock
import com.kennyc.solarviewer.data.SolarRepository
import com.kennyc.solarviewer.data.model.ConsumptionStats
import com.kennyc.solarviewer.data.model.ProductionStats
import com.kennyc.solarviewer.data.model.SolarGraphData
import com.kennyc.solarviewer.data.model.SolarSystem
import com.kennyc.solarviewer.utils.ContentState
import com.kennyc.solarviewer.utils.ErrorState
import com.kennyc.solarviewer.utils.RxUtils.asLiveData
import com.kennyc.solarviewer.utils.RxUtils.observeChain
import com.kennyc.solarviewer.utils.UiState
import com.kennyc.solarviewer.utils.toNegative
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DailyViewModel @Inject constructor(
    private val repo: SolarRepository,
    private val clock: Clock
) : ViewModel() {
    private val selectedDate = BehaviorSubject.create<Date>()

    private val selectedSystem = BehaviorSubject.create<SolarSystem>()

    private val _selectedBarPoint = PublishSubject.create<BarPoint>()

    val state: LiveData<UiState> =
        Observable.combineLatest(selectedDate, selectedSystem, { date, system ->
            val startTime = clock.midnight(date)
            val endDay = (startTime + TimeUnit.HOURS.toMillis(24))
                .takeIf { !clock.isInFuture(it) }

            Triple(system, startTime, endDay)
        }).flatMapSingle {
            repo.getProductionStats(it.first, it.second, it.third)
                .zipWith(repo.getConsumptionStats(it.first, it.second, it.third),
                    { production, consumption ->
                        buildData(consumption, production)
                    })
        }.observeChain()
            .map { ContentState(it) as UiState }
            .onErrorReturn { ErrorState(it) }
            .asLiveData()


    val selectedBarPoint = _selectedBarPoint.asLiveData()

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
        selectedSystem.onNext(system)
    }

    fun setSelectedDate(date: Date) {
        selectedDate.onNext(date)
    }

    fun setSelectedBarPoint(point: BarPoint) {
        _selectedBarPoint.onNext(point)
    }

    fun refresh(date: Date? = null) {
        setSelectedDate(date ?: clock.currentDate())
    }
}

// The amount of items a full day of stats should contain
private const val DAY_STAT_SIZE = 96