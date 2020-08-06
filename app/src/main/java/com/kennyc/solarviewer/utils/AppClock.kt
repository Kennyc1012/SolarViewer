package com.kennyc.solarviewer.utils

import com.kennyc.solarviewer.data.Clock
import java.time.ZoneId
import java.util.*
import java.util.concurrent.TimeUnit

class AppClock : Clock {

    override fun currentDate(): Date = Date(currentTime())

    override fun currentTime(): Long = System.currentTimeMillis()

    override fun midnight(date: Date): Long =
        TimeUnit.SECONDS.toMillis(
            date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .atStartOfDay(ZoneId.systemDefault())
                .toEpochSecond()
        )

    override fun isInFuture(date: Date): Boolean = isInFuture(date.time)

    override fun isInFuture(time: Long): Boolean = time > currentTime()
}