package com.kennyc.api_enphase.model

import com.google.gson.annotations.SerializedName

data class EnphaseProductionStats(
    @SerializedName("end_at") val endingAtTS: Long,
    @SerializedName("wh_del") val powerCreatedInWh: Int
)