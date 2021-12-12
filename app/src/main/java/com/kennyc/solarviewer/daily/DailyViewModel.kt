package com.kennyc.solarviewer.daily

import androidx.lifecycle.ViewModel
import com.kennyc.solarviewer.data.Clock
import com.kennyc.solarviewer.data.SolarRepository
import com.kennyc.solarviewer.data.model.ConsumptionStats
import com.kennyc.solarviewer.data.model.EMPTY_SYSTEM
import com.kennyc.solarviewer.data.model.ProductionStats
import com.kennyc.solarviewer.data.model.SolarGraphData
import com.kennyc.solarviewer.utils.*
import com.kennyc.solarviewer.utils.RxUtils.observeChain
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DailyViewModel @Inject constructor(
    private val repo: SolarRepository,
    private val clock: Clock
) : ViewModel() {
    private var disposable: Disposable? = null
    private val barSubject = PublishSubject.create<BarPoint>()
    val selectedBarPoint: Observable<BarPoint> = barSubject

    private val uiSubject: PublishSubject<UiState> = PublishSubject.create()

    val state: Observable<UiState> = uiSubject.defaultIfEmpty(LoadingState)

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

    fun setSelectedBarPoint(point: BarPoint) {
        barSubject.onNext(point)
    }

    fun refresh() {
        disposable?.dispose()

        val dateObservable = repo.selectedDate()
            .doOnNext {
                uiSubject.onNext(LoadingState)
            }

        val systemObservable = repo.selectedSystem()
            .filter { it != EMPTY_SYSTEM }
            .doOnNext {
                uiSubject.onNext(LoadingState)
            }

        disposable = Observable.combineLatest(dateObservable, systemObservable, { date, system ->
            val start = clock.midnight(date)
            val end = (start + TimeUnit.HOURS.toMillis(24))
                .takeIf { !clock.isInFuture(it) }

            repo.getProductionStats(system, start, end)
                .zipWith(repo.getConsumptionStats(system, start, end),
                    { production, consumption ->
                        buildData(consumption, production)
                    })
        }).flatMapSingle { it }
            .observeChain()
            .map {
                val state: UiState = ContentState(it)
                state
            }
            .onErrorReturn { ErrorState(it) }
            .subscribe { state -> uiSubject.onNext(state) }
    }

    override fun onCleared() {
        super.onCleared()
        disposable?.dispose()
    }
}

// The amount of items a full day of stats should contain
private const val DAY_STAT_SIZE = 96