package com.kennyc.solarviewer.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

class MultiMediatorLiveData<T> : MediatorLiveData<T>() {

    fun <A, B> addSources(ld1: LiveData<A>, ld2: LiveData<B>, onChanged: (A, B) -> Unit) {
        addSource(ld1) {
            val other = ld2.value
            if (other != null) onChanged.invoke(it, other)
        }

        addSource(ld2) {
            val other = ld1.value
            if (other != null) onChanged.invoke(other, it)
        }
    }

}