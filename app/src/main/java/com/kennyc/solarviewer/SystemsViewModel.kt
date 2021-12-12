package com.kennyc.solarviewer

import androidx.lifecycle.ViewModel
import com.kennyc.solarviewer.data.Clock
import com.kennyc.solarviewer.data.LocalSettings
import com.kennyc.solarviewer.data.SolarRepository
import com.kennyc.solarviewer.data.model.SolarSystem
import com.kennyc.solarviewer.utils.RxUtils.observeChain
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import java.util.*
import javax.inject.Inject

class SystemsViewModel @Inject constructor(
    localSettings: LocalSettings,
    private val repo: SolarRepository,
    private val clock: Clock
) : ViewModel() {

    private val disposables = CompositeDisposable()

    val currentTime: Date get() = clock.currentDate()

    val systems: Observable<List<SolarSystem>> = repo.getSolarSystems()

    val selectedSystem = repo.selectedSystem()

    val selectedDate = repo.selectedDate()

    init {
        val d =localSettings
            .getStoredString(KEY_LAST_USED_SYSTEM, DEFAULT_VALUE)
            .toObservable()
            .flatMap { lastUsed ->
                repo.getSolarSystems()
                    .map { systems ->
                        lastUsed
                            // Get last stored system
                            .takeIf { id -> id != DEFAULT_VALUE }
                            ?.let { id ->
                                // Return the system with the same id
                                systems.firstOrNull { s ->
                                    s.id == id
                                }
                                // If not found return first item
                            } ?: systems.first()
                    }
            }.observeChain()
            .subscribe {
                repo.currentSystem = it
            }

        setNewDate(currentTime)
        disposables.add(d)
    }

    fun setNewDate(newDate: Date) {
        repo.currentDate = newDate
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}

private const val KEY_LAST_USED_SYSTEM = "KEY_LAST_USED_SYSTEM"
private const val DEFAULT_VALUE = "SystemsViewModel.NONE"