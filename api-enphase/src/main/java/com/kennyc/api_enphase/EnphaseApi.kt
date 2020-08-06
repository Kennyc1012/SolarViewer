package com.kennyc.api_enphase

import com.kennyc.api_enphase.model.EnphaseSystemSummary
import com.kennyc.api_enphase.response.EnphaseConsumptionResponse
import com.kennyc.api_enphase.response.EnphaseProductionResponse
import com.kennyc.api_enphase.response.EnphaseSystemsResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface EnphaseApi {

    @GET("systems")
    suspend fun getSystems(): EnphaseSystemsResponse

    @GET("systems/{systemId}/summary")
    suspend fun getSystemSummary(@Path("systemId") systemId: String): EnphaseSystemSummary

    @GET("systems/{systemId}/rgm_stats")
    suspend fun getProductionStats(
        @Path("systemId") systemId: String,
        @Query("start_at") startTimeInSeconds: Long?,
        @Query("end_at") endTimeInSeconds: Long?
    ): EnphaseProductionResponse

    @GET("systems/{systemId}/consumption_stats")
    suspend fun getConsumptionStats(
        @Path("systemId") systemId: String,
        @Query("start_at") startTimeInSeconds: Long?,
        @Query("end_at") endTimeInSeconds: Long?
    ): EnphaseConsumptionResponse
}