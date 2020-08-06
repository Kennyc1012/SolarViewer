package com.kennyc.api_enphase.model

import com.google.gson.annotations.SerializedName

data class EnphaseSystem(
    @SerializedName("system_id") val id: String,
    @SerializedName("system_name") val name: String,
    @SerializedName("status") val status: String
)