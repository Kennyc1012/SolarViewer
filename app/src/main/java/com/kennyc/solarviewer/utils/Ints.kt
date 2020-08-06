package com.kennyc.solarviewer.utils

import java.text.DecimalFormat

fun Int.asKilowattString(): String = DecimalFormat("##.##").format(asKilowatt())

fun Int.asKilowatt(): Double = this.toDouble() / 1000

fun Int.toNegative(): Int = this * -1
