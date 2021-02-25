package com.kennyc.api_enphase

import com.kennyc.api_enphase.model.EnphaseSystemSummary
import com.kennyc.api_enphase.response.EnphaseConsumptionResponse
import com.kennyc.api_enphase.response.EnphaseProductionResponse
import com.kennyc.api_enphase.response.EnphaseSystemsResponse
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface EnphaseApi {

    @GET("systems")
    fun getSystems(): Single<EnphaseSystemsResponse>

    @GET("systems/{systemId}/summary")
    fun getSystemSummary(@Path("systemId") systemId: String): Single<EnphaseSystemSummary>

    @GET("systems/{systemId}/rgm_stats")
    fun getProductionStats(
        @Path("systemId") systemId: String,
        @Query("start_at") startTimeInSeconds: Long?,
        @Query("end_at") endTimeInSeconds: Long?
    ): Single<EnphaseProductionResponse>

    @GET("systems/{systemId}/consumption_stats")
    fun getConsumptionStats(
        @Path("systemId") systemId: String,
        @Query("start_at") startTimeInSeconds: Long?,
        @Query("end_at") endTimeInSeconds: Long?
    ): Single<EnphaseConsumptionResponse>
}