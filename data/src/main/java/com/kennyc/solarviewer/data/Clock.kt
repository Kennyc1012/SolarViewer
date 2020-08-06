package com.kennyc.solarviewer.data

import java.util.*

// All times will be in Milliseconds
interface Clock {

    fun currentDate(): Date

    fun currentTime(): Long

    fun midnight(date: Date): Long

    fun isInFuture(time: Long): Boolean

    fun isInFuture(date: Date): Boolean
}