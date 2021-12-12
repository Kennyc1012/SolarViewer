package com.kennyc.solarviewer.daily

import java.util.*

data class BarPoint(val produced: Float, val consumed: Float, val date: Date) {
    companion object {
        val EMPTY_POINT = BarPoint(0f, 0f, Date())
    }
}