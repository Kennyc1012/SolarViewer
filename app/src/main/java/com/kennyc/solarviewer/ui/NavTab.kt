package com.kennyc.solarviewer.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kennyc.solarviewer.R

sealed class NavTab(val route: String, @DrawableRes val icon: Int, @StringRes val title: Int) {
    object Home : NavTab("Home", R.drawable.ic_baseline_home_24, R.string.tab_home)
    object Daily : NavTab("Daily", R.drawable.ic_baseline_calendar_24, R.string.tab_daily)
}