package com.kennyc.solarviewer

import androidx.lifecycle.*
import com.kennyc.solarviewer.data.Clock
import com.kennyc.solarviewer.data.LocalSettings
import com.kennyc.solarviewer.data.SolarRepository
import com.kennyc.solarviewer.data.model.CoroutineDispatchProvider
import com.kennyc.solarviewer.data.model.SolarSystem
import java.util.*
import javax.inject.Inject

class SystemsViewModel @Inject constructor(
    private val localSettings: LocalSettings,
    repo: SolarRepository,
    provider: CoroutineDispatchProvider,
    clock: Clock
) : ViewModel() {

    val date: LiveData<Date> = liveData(provider.main) {
        emit(clock.currentDate())
    }

    val systems: LiveData<List<SolarSystem>> = liveData(provider.io) {
        emit(repo.getSolarSystems())
    }

    val selectedSystem = MediatorLiveData<SolarSystem>().apply {
        addSource(systems) { retrievedSystems ->
            removeSource(systems)
            value = localSettings.getStoredString(KEY_LAST_USED_SYSTEM)
                // Get last stored system
                .takeIf { id -> !id.isNullOrBlank() }
                ?.let { id ->
                    // Return the system with the same id
                    retrievedSystems.firstOrNull { s ->
                        s.id == id
                    }
                    // If not found return first item
                } ?: retrievedSystems.first()
        }
    }

    fun onSystemSelected(solarSystem: SolarSystem) {
        selectedSystem.value = solarSystem
        localSettings.store(KEY_LAST_USED_SYSTEM, solarSystem.id)
    }

    fun setNewDate(newDate: Date) {
        date as MutableLiveData<Date>
        date.value = newDate
    }
}

private const val KEY_LAST_USED_SYSTEM = "KEY_LAST_USED_SYSTEM"