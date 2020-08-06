package com.kennyc.solarviewer

import androidx.lifecycle.*
import com.kennyc.solarviewer.data.Clock
import com.kennyc.solarviewer.data.SolarRepository
import com.kennyc.solarviewer.data.model.CoroutineDispatchProvider
import com.kennyc.solarviewer.data.model.SolarSystem
import java.util.*
import javax.inject.Inject

class SystemsViewModel @Inject constructor(
    repo: SolarRepository,
    provider: CoroutineDispatchProvider,
    clock: Clock
) : ViewModel() {

    val date: LiveData<Date> = MutableLiveData<Date>().apply {
        value = clock.currentDate()
    }

    val systems: LiveData<List<SolarSystem>> = liveData(provider.io) {
        emit(repo.getSolarSystems())
    }

    // TODO Save last selected as preference
    val selectedSystem = MediatorLiveData<SolarSystem>().apply {
        addSource(systems) {
            removeSource(systems)
            value = it.first()
        }
    }

    fun onSystemSelected(solarSystem: SolarSystem) {
        selectedSystem.value = solarSystem
    }

    fun setNewDate(newDate: Date) {
        date as MutableLiveData<Date>
        date.value = newDate
    }
}