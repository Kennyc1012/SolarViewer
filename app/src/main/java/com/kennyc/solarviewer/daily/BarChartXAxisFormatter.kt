package com.kennyc.solarviewer.daily

import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*

class BarChartXAxisFormatter(private val times: MutableList<Date>) : ValueFormatter() {

    private val formatter = SimpleDateFormat("ha", Locale.getDefault()).apply {
        timeZone = TimeZone.getDefault()
    }

    override fun getFormattedValue(value: Float): String {
        if (times.isEmpty()) return ""

        return times[value.toInt()].let { formatter.format(it) }
    }
}