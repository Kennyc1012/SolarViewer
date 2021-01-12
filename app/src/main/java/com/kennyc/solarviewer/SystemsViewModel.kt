package com.kennyc.solarviewer

import androidx.lifecycle.*
import com.kennyc.solarviewer.data.Clock
import com.kennyc.solarviewer.data.LocalSettings
import com.kennyc.solarviewer.data.SolarRepository
import com.kennyc.solarviewer.data.model.CoroutineDispatchProvider
import com.kennyc.solarviewer.data.model.SolarSystem
import com.kennyc.solarviewer.utils.MultiMediatorLiveData
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class SystemsViewModel @Inject constructor(
    private val localSettings: LocalSettings,
    repo: SolarRepository,
    private val provider: CoroutineDispatchProvider,
    clock: Clock
) : ViewModel() {

    val date: LiveData<Date> = liveData(provider.main) {
        emit(clock.currentDate())
    }

    val systems: LiveData<List<SolarSystem>> = liveData(provider.io) {
        emit(repo.getSolarSystems())
    }

    private val lastUsedSystemsViewModel: LiveData<String> =
        localSettings.getStoredString(KEY_LAST_USED_SYSTEM, DEFAULT_VALUE)
            .asLiveData(provider.io)

    val selectedSystem = MultiMediatorLiveData<SolarSystem>().apply {
        addSources(systems, lastUsedSystemsViewModel)
        { sys, last ->
            removeSource(lastUsedSystemsViewModel)
            removeSource(systems)

            value = last
                // Get last stored system
                .takeIf { id -> id != DEFAULT_VALUE }
                ?.let { id ->
                    // Return the system with the same id
                    sys.firstOrNull { s ->
                        s.id == id
                    }
                    // If not found return first item
                } ?: sys.first()
        }
    }

    fun onSystemSelected(solarSystem: SolarSystem) {
        selectedSystem.value = solarSystem

        viewModelScope.launch(provider.io) {
            localSettings.store(KEY_LAST_USED_SYSTEM, solarSystem.id)
        }
    }

    fun setNewDate(newDate: Date) {
        date as MutableLiveData<Date>
        date.value = newDate
    }
}

private const val KEY_LAST_USED_SYSTEM = "KEY_LAST_USED_SYSTEM"
private const val DEFAULT_VALUE = "SystemsViewModel.NONE"