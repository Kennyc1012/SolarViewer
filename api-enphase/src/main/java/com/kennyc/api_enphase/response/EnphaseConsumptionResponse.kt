package com.kennyc.api_enphase.response

import com.google.gson.annotations.SerializedName
import com.kennyc.api_enphase.model.EnphaseConsumptionStats

data class EnphaseConsumptionResponse(@SerializedName("intervals") val stats: List<EnphaseConsumptionStats>)