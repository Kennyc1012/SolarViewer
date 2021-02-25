package com.kennyc.solarviewer.data.rx

import io.reactivex.rxjava3.core.CompletableObserver
import io.reactivex.rxjava3.disposables.Disposable

class CompletableSubscriber : CompletableObserver {

    companion object {
        fun stub() = CompletableSubscriber()
    }

    override fun onSubscribe(d: Disposable) {
        // NOOP
    }

    override fun onComplete() {
        // NOOP
    }

    override fun onError(e: Throwable) {
        // NOOP
    }
}