package com.kennyc.api_enphase.model

import com.google.gson.annotations.SerializedName

data class EnphaseSystemSummary(
    @SerializedName("system_id") val systemId: String,
    @SerializedName("modules") val numModules: Int,
    @SerializedName("size_w") val sizeInWatts: Int,
    @SerializedName("current_power") val currentPowerInWatts: Int,
    @SerializedName("energy_today") val energyTodayInWatts: Int,
    @SerializedName("energy_lifetime") val energyLifetimeInWatts: Int,
    @SerializedName("status") val status: String,
    @SerializedName("last_report_at") val lastReportTS: Long
)