package com.kennyc.solarviewer.daily

import com.github.mikephil.charting.formatter.ValueFormatter
import kotlin.math.abs

class BarChartYAxisFormatter : ValueFormatter() {
    override fun getFormattedValue(value: Float): String = abs(value.toInt()).toString() + "wH"
}