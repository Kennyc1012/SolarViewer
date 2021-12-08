package com.kennyc.solarviewer

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kennyc.solarviewer.data.Clock
import com.kennyc.solarviewer.data.LocalSettings
import com.kennyc.solarviewer.data.SolarRepository
import com.kennyc.solarviewer.data.model.SolarSystem
import com.kennyc.solarviewer.data.rx.CompletableSubscriber
import com.kennyc.solarviewer.utils.MultiMediatorLiveData
import com.kennyc.solarviewer.utils.RxUtils.asLiveData
import com.kennyc.solarviewer.utils.RxUtils.observeChain
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.util.*
import javax.inject.Inject

class SystemsViewModel @Inject constructor(
    private val localSettings: LocalSettings,
    repo: SolarRepository,
    private val clock: Clock
) : ViewModel() {

    val currentTime: Date get() =  clock.currentDate()

    private val dateSubject = BehaviorSubject.create<Date>()
    val date: LiveData<Date> = dateSubject.asLiveData()

    val systems: LiveData<List<SolarSystem>> = repo.getSolarSystems()
        .observeChain()
        .asLiveData()

    private val lastUsedSystem: LiveData<String> =
        localSettings.getStoredString(KEY_LAST_USED_SYSTEM, DEFAULT_VALUE)
            .observeChain()
            .asLiveData()

    val selectedSystem = MultiMediatorLiveData<SolarSystem>().apply {
        addSources(systems, lastUsedSystem)
        { sys, last ->
            removeSource(lastUsedSystem)
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

    init {
        dateSubject.onNext(clock.currentDate())
    }

    fun onSystemSelected(solarSystem: SolarSystem) {
        selectedSystem.value = solarSystem

        localSettings.store(KEY_LAST_USED_SYSTEM, solarSystem.id)
            .observeChain()
            .subscribe(CompletableSubscriber.stub())
    }

    fun setNewDate(newDate: Date) {
        dateSubject.onNext(newDate)
    }
}

private const val KEY_LAST_USED_SYSTEM = "KEY_LAST_USED_SYSTEM"
private const val DEFAULT_VALUE = "SystemsViewModel.NONE"