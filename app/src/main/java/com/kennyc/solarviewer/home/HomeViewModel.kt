package com.kennyc.solarviewer.home

import androidx.lifecycle.ViewModel
import com.kennyc.solarviewer.data.Clock
import com.kennyc.solarviewer.data.SolarRepository
import com.kennyc.solarviewer.data.model.EMPTY_SYSTEM
import com.kennyc.solarviewer.utils.ContentState
import com.kennyc.solarviewer.utils.ErrorState
import com.kennyc.solarviewer.utils.LoadingState
import com.kennyc.solarviewer.utils.RxUtils.observeChain
import com.kennyc.solarviewer.utils.UiState
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class HomeViewModel @Inject constructor(
    private val repo: SolarRepository,
    private val clock: Clock
) : ViewModel() {
    private var disposable: Disposable? = null
    private val uiSubject: PublishSubject<UiState> = PublishSubject.create()

    val state: Observable<UiState> = uiSubject.defaultIfEmpty(LoadingState)
        .doOnSubscribe { refresh() }

    fun refresh() {
        disposable?.dispose()

        val dateObservable = repo.selectedDate()
            .doOnNext {
                uiSubject.onNext(LoadingState)
            }

        val systemObservable = repo.selectedSystem()
            .filter { it != EMPTY_SYSTEM }
            .doOnNext {
                uiSubject.onNext(LoadingState)
            }

        disposable = Observable.combineLatest(dateObservable, systemObservable,
            { date, system ->
                val start = clock.midnight(date)
                val end = (start + TimeUnit.HOURS.toMillis(24))
                    .takeIf { time -> !clock.isInFuture(time) }

                repo.getSystemReport(system, start, end)
            }).flatMapSingle { it }
            .observeChain()
            .map {
                val state: UiState = ContentState(it) as UiState
                state
            }
            .onErrorReturn { ErrorState(it) }
            .subscribe { state -> uiSubject.onNext(state) }
    }

    override fun onCleared() {
        super.onCleared()
        disposable?.dispose()
    }
}