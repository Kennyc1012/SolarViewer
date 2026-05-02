package com.kennyc.api_enphase.auth

import com.google.gson.annotations.SerializedName

data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("expires_in") val expiresIn: Long,
    @SerializedName("enl_uid") val enphaseUserId: String? = null,
    @SerializedName("enl_cid") val enphaseCustomerId: String? = null
)
