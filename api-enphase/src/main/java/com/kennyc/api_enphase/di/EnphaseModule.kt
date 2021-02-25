package com.kennyc.api_enphase.di

import com.kennyc.api_enphase.EnphaseApi
import com.kennyc.api_enphase.intercepor.ErrorInterceptor
import com.kennyc.api_enphase.intercepor.IdInterceptor
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
class EnphaseModule {

    @Provides
    @Singleton
    fun providesOkHttp(
        idInterceptor: IdInterceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(idInterceptor)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(ErrorInterceptor())
            .build()
    }

    @Provides
    @Singleton
    fun providesApi(okHttpClient: OkHttpClient): EnphaseApi =
        Retrofit.Builder()
            .baseUrl("https://api.enphaseenergy.com/api/v2/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build()
            .create(EnphaseApi::class.java)

    @Provides
    fun providesLogInterceptor(@Named("isDebug") isDebug: Boolean): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = when (isDebug) {
                true -> HttpLoggingInterceptor.Level.BODY
                else -> HttpLoggingInterceptor.Level.NONE
            }
        }

    @Provides
    fun providesIdInterceptor(
        @Named("appId") appId: String,
        @Named("userId") userId: String
    ): IdInterceptor = IdInterceptor(appId, userId)
}