package com.kennyc.solarviewer.di.modules

import android.content.Context
import com.kennyc.api_enphase.EnphaseApi
import com.kennyc.api_enphase.auth.AuthApi
import com.kennyc.api_enphase.auth.TokenStore
import com.kennyc.data_enphase.EnphaseSolarRepository
import com.kennyc.data_enphase.auth.EnphaseAuthManager
import com.kennyc.data_store.PreferenceStore
import com.kennyc.solarviewer.BuildConfig
import com.kennyc.solarviewer.data.AuthManager
import com.kennyc.solarviewer.data.Clock
import com.kennyc.solarviewer.data.LocalSettings
import com.kennyc.solarviewer.data.Logger
import com.kennyc.solarviewer.data.SolarRepository
import com.kennyc.solarviewer.utils.AppClock
import com.kennyc.solarviewer.utils.AppLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun providesLogger(): Logger = AppLogger(BuildConfig.DEBUG)

    @Provides
    @Singleton
    fun providesPreferenceStore(@ApplicationContext context: Context): PreferenceStore =
        PreferenceStore(context)

    @Provides
    @Singleton
    fun providesTokenStore(preferenceStore: PreferenceStore): TokenStore = preferenceStore

    @Provides
    @Singleton
    fun providesSolarRepository(
        @ApplicationContext context: Context,
        api: EnphaseApi,
        logger: Logger,
        clock: Clock
    ): SolarRepository = EnphaseSolarRepository(context, clock, api, logger)

    @Provides
    @Singleton
    fun providesAuthManager(
        authApi: AuthApi,
        tokenStore: TokenStore,
        @Named("clientId") clientId: String,
        @Named("clientSecret") clientSecret: String,
        @Named("redirectUri") redirectUri: String,
    ): AuthManager = EnphaseAuthManager(
        authApi = authApi,
        tokenStore = tokenStore,
        clientId = clientId,
        clientSecret = clientSecret,
        redirectUri = redirectUri
    )

    @Provides
    @Singleton
    fun providesClock(): Clock = AppClock()

    @Provides
    @Singleton
    fun providesLocalSettings(preferenceStore: PreferenceStore): LocalSettings = preferenceStore

    @Provides
    @Named("apiKey")
    fun providesApiKey(): String = BuildConfig.API_KEY

    @Provides
    @Named("clientId")
    fun providesClientId(): String = BuildConfig.CLIENT_ID

    @Provides
    @Named("clientSecret")
    fun providesClientSecret(): String = BuildConfig.CLIENT_SECRET

    @Provides
    @Named("isDebug")
    fun providesIsDebug(): Boolean = BuildConfig.DEBUG

    @Provides
    @Named("redirectUri")
    fun providesRedirectUri(): String = BuildConfig.REDIRECT_URI
}
