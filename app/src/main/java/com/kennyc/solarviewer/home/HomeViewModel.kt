package com.kennyc.solarviewer.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kennyc.solarviewer.data.Clock
import com.kennyc.solarviewer.data.SolarRepository
import com.kennyc.solarviewer.data.model.SolarSystem
import com.kennyc.solarviewer.data.model.SolarSystemReport
import com.kennyc.solarviewer.utils.RxUtils.asLiveData
import com.kennyc.solarviewer.utils.RxUtils.observeChain
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class HomeViewModel @Inject constructor(
    private val repo: SolarRepository,
    private val clock: Clock
) : ViewModel() {

    val dateError = MutableLiveData<Unit>()

    val rateLimitError = MutableLiveData<Unit>()

    private val selectedDate = BehaviorSubject.create<Date>()

    private val selectedSystem = BehaviorSubject.create<SolarSystem>()

    val summary: LiveData<SolarSystemReport> =
        Observable.combineLatest(selectedDate, selectedSystem, { date, system ->
            val start = clock.midnight(date)
            val end = (start + TimeUnit.HOURS.toMillis(24))
                .takeIf { !clock.isInFuture(it) }

            Triple(system, start, end)
        }).flatMapSingle { repo.getSystemReport(it.first, it.second, it.third) }
            .observeChain()
            .asLiveData()

    /*
    * TODO Handle errors
    *   when (e) {
                        is RateLimitException -> withContext(provider.main) {
                            rateLimitError.value = Unit
                        }

                        else -> withContext(provider.main) {
                            dateError.value = Unit
                        }
                    }
    * */

    fun setSelectedSystem(system: SolarSystem) {
        selectedSystem.onNext(system)
    }

    fun setSelectedDate(date: Date) {
        selectedDate.onNext(date)
    }
}