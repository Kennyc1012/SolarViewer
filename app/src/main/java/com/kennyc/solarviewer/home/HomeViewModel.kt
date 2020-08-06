package com.kennyc.solarviewer.home

import androidx.lifecycle.*
import com.kennyc.solarviewer.data.Clock
import com.kennyc.solarviewer.data.SolarRepository
import com.kennyc.solarviewer.data.model.CoroutineDispatchProvider
import com.kennyc.solarviewer.data.model.SolarSystem
import com.kennyc.solarviewer.data.model.SolarSystemReport
import com.kennyc.solarviewer.data.model.exception.RateLimitException
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
class HomeViewModel @Inject constructor(
    repo: SolarRepository,
    clock: Clock,
    private val provider: CoroutineDispatchProvider
) : ViewModel() {

    val dateError = MutableLiveData<Unit>()

    val rateLimitError = MutableLiveData<Unit>()

    private val selectedDate = BroadcastChannel<Date>(1)

    private val selectedSystem = BroadcastChannel<SolarSystem>(1)

    // TODO Handle errors
    val summary: LiveData<SolarSystemReport> = selectedDate.asFlow()
        .combine(selectedSystem.asFlow()) { date, system ->
            val start = clock.midnight(date)
            val end = (start + TimeUnit.HOURS.toMillis(24))
                .takeIf { !clock.isInFuture(it) }

            repo.getSystemReport(system, start, end)
        }.catch {
            when (it) {
                is RateLimitException -> withContext(provider.main) {
                    rateLimitError.value = Unit
                }

                else -> withContext(provider.main) {
                    dateError.value = Unit
                }
            }
        }
        .asLiveData(viewModelScope.coroutineContext, Duration.ofSeconds(10))

    fun setSelectedSystem(system: SolarSystem) = selectedSystem.offer(system)

    fun setSelectedDate(date: Date) = selectedDate.offer(date)
}