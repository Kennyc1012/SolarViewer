package com.kennyc.solarviewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kennyc.solarviewer.data.Clock
import com.kennyc.solarviewer.data.LocalSettings
import com.kennyc.solarviewer.data.SolarRepository
import com.kennyc.solarviewer.data.model.EMPTY_SYSTEM
import com.kennyc.solarviewer.data.model.SolarSystem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

class SystemsViewModel @Inject constructor(
    localSettings: LocalSettings,
    private val repo: SolarRepository,
    private val clock: Clock
) : ViewModel() {

    val currentTime: Date get() = clock.currentDate()

    val systems: Flow<List<SolarSystem>> = repo.getSolarSystems()

    val selectedSystem: StateFlow<SolarSystem> = repo.selectedSystem()

    val selectedDate: StateFlow<Date> = repo.selectedDate()

    init {
        viewModelScope.launch {
            val lastUsed = localSettings.getStoredString(KEY_LAST_USED_SYSTEM, DEFAULT_VALUE)
            val systems = repo.getSolarSystems().first()
            val pick = lastUsed.takeIf { it != DEFAULT_VALUE }
                ?.let { id -> systems.firstOrNull { it.id == id } }
                ?: systems.firstOrNull()
                ?: EMPTY_SYSTEM
            repo.currentSystem = pick
        }
        setNewDate(currentTime)
    }

    fun setNewDate(newDate: Date) {
        repo.currentDate = newDate
    }
}

private const val KEY_LAST_USED_SYSTEM = "KEY_LAST_USED_SYSTEM"
private const val DEFAULT_VALUE = "SystemsViewModel.NONE"
