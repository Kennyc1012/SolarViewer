package com.kennyc.api_enphase.response

import com.google.gson.annotations.SerializedName
import com.kennyc.api_enphase.model.EnphaseSystem

data class EnphaseSystemsResponse(@SerializedName("systems") val systems: List<EnphaseSystem>)