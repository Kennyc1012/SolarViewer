package com.kennyc.solarviewer.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.toLiveData
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.Subject

object RxUtils {

    fun <T> Flowable<T>.asLiveData(): LiveData<T> {
        return LiveDataReactiveStreams.fromPublisher(this)
    }

    fun <T> Subject<T>.asLiveData(): LiveData<T> =
        toFlowable(BackpressureStrategy.LATEST).toLiveData()

    fun <T> Observable<T>.asLiveData(): LiveData<T> =
        toFlowable(BackpressureStrategy.LATEST).toLiveData()

    fun <T> Single<T>.asLiveData(): LiveData<T> = toFlowable().asLiveData()

    fun <T> Observable<T>.observeChain(): Observable<T> =
        subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

    fun <T> Subject<T>.observeChain(): Observable<T> =
        subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

    fun <T> Single<T>.observeChain(): Single<T> =
        subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

    fun <T> Maybe<T>.observeChain(): Maybe<T> =
        subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

    fun <T> Flowable<T>.observeChain(): Flowable<T> =
        subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

    fun Completable.observeChain(): Completable =
        subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
}