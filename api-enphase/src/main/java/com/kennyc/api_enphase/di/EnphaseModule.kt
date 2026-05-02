package com.kennyc.api_enphase.di

import com.kennyc.api_enphase.EnphaseApi
import com.kennyc.api_enphase.auth.AuthApi
import com.kennyc.api_enphase.auth.TokenStore
import com.kennyc.api_enphase.intercepor.AuthInterceptor
import com.kennyc.api_enphase.intercepor.ErrorInterceptor
import com.kennyc.api_enphase.intercepor.TokenAuthenticator
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
    @Named("auth")
    fun providesAuthOkHttp(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun providesAuthApi(@Named("auth") okHttpClient: OkHttpClient): AuthApi =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build()
            .create(AuthApi::class.java)

    @Provides
    @Singleton
    fun providesAuthInterceptor(
        @Named("apiKey") apiKey: String,
        tokenStore: TokenStore
    ): AuthInterceptor = AuthInterceptor(apiKey, tokenStore)

    @Provides
    @Singleton
    fun providesTokenAuthenticator(
        authApi: AuthApi,
        tokenStore: TokenStore,
        @Named("clientId") clientId: String,
        @Named("clientSecret") clientSecret: String
    ): TokenAuthenticator = TokenAuthenticator(authApi, tokenStore, clientId, clientSecret)

    @Provides
    @Singleton
    fun providesOkHttp(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(ErrorInterceptor())
            .authenticator(tokenAuthenticator)
            .build()
    }

    @Provides
    @Singleton
    fun providesApi(okHttpClient: OkHttpClient): EnphaseApi =
        Retrofit.Builder()
            .baseUrl("${BASE_URL}api/v4/")
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

    private companion object {
        const val BASE_URL = "https://api.enphaseenergy.com/"
    }
}
