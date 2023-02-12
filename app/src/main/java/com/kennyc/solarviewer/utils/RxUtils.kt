package com.kennyc.solarviewer.utils

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.Subject

object RxUtils {

    fun <T : Any> Observable<T>.observeChain(): Observable<T> =
        subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

    fun <T : Any> Subject<T>.observeChain(): Observable<T> =
        subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

    fun <T : Any> Single<T>.observeChain(): Single<T> =
        subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

    fun <T> Maybe<T>.observeChain(): Maybe<T> =
        subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

    fun <T : Any> Flowable<T>.observeChain(): Flowable<T> =
        subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

    fun Completable.observeChain(): Completable =
        subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
}