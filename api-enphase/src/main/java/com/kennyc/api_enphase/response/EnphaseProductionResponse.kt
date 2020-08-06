package com.kennyc.api_enphase.response

import com.google.gson.annotations.SerializedName
import com.kennyc.api_enphase.model.EnphaseProductionStats

data class EnphaseProductionResponse(@SerializedName("intervals") val stats: List<EnphaseProductionStats>)