package com.kennyc.api_enphase.model

import com.google.gson.annotations.SerializedName

data class EnphaseConsumptionStats(
    @SerializedName("end_at") val endingAtTS: Long,
    @SerializedName("enwh") val powerConsumedInWh: Int
)