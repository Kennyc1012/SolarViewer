package com.kennyc.solarviewer.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kennyc.solarviewer.data.Clock
import com.kennyc.solarviewer.data.SolarRepository
import com.kennyc.solarviewer.data.model.CoroutineDispatchProvider
import com.kennyc.solarviewer.data.model.SolarSystem
import com.kennyc.solarviewer.data.model.SolarSystemReport
import com.kennyc.solarviewer.data.model.exception.RateLimitException
import com.kennyc.solarviewer.utils.MultiMediatorLiveData
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class HomeViewModel @Inject constructor(
    private val provider: CoroutineDispatchProvider,
    private val repo: SolarRepository,
    private val clock: Clock
) : ViewModel() {

    val dateError = MutableLiveData<Unit>()

    val rateLimitError = MutableLiveData<Unit>()

    private val selectedDate = MutableLiveData<Date>()

    private val selectedSystem = MutableLiveData<SolarSystem>()

    val summary: LiveData<SolarSystemReport> = MultiMediatorLiveData<SolarSystemReport>().apply {
        addSources(selectedDate, selectedSystem) { date,system ->
            viewModelScope.launch(provider.io) {
                try {
                    val report = fetchReport(date,system)
                    withContext(provider.main) {
                        value = report
                    }
                } catch (e: Exception) {
                    when (e) {
                        is RateLimitException -> withContext(provider.main) {
                            rateLimitError.value = Unit
                        }

                        else -> withContext(provider.main) {
                            dateError.value = Unit
                        }
                    }
                }
            }
        }
    }

    fun setSelectedSystem(system: SolarSystem) {
        selectedSystem.value = system
    }

    fun setSelectedDate(date: Date) {
        selectedDate.value = date
    }

    private suspend fun fetchReport(date: Date, system: SolarSystem): SolarSystemReport {
        val start = clock.midnight(date)
        val end = (start + TimeUnit.HOURS.toMillis(24))
            .takeIf { !clock.isInFuture(it) }

        return repo.getSystemReport(system, start, end)
    }
}