package com.kennyc.solarviewer.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.kennyc.solarviewer.data.Clock
import com.kennyc.solarviewer.data.SolarRepository
import com.kennyc.solarviewer.data.model.EMPTY_SYSTEM
import com.kennyc.solarviewer.utils.ContentState
import com.kennyc.solarviewer.utils.ErrorState
import com.kennyc.solarviewer.utils.LoadingState
import com.kennyc.solarviewer.utils.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: SolarRepository,
    private val clock: Clock
) : ViewModel() {

    private val _state = MutableStateFlow<UiState>(LoadingState)
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var job: Job? = null

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
                        .takeIf { time -> !clock.isInFuture(time) }
                    _state.value = try {
                        ContentState(repo.getSystemReport(system, start, end))
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
