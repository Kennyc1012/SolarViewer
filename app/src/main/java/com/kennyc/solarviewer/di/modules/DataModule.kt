package com.kennyc.solarviewer.di.modules

import android.content.Context
import com.kennyc.api_enphase.auth.AuthApi
import com.kennyc.api_enphase.auth.TokenStore
import com.kennyc.api_enphase.di.EnphaseComponent
import com.kennyc.data_enphase.EnphaseSolarRepository
import com.kennyc.data_store.PreferenceStore
import com.kennyc.solarviewer.BuildConfig
import com.kennyc.solarviewer.data.Clock
import com.kennyc.solarviewer.data.LocalSettings
import com.kennyc.solarviewer.data.Logger
import com.kennyc.solarviewer.data.SolarRepository
import com.kennyc.solarviewer.utils.AppClock
import com.kennyc.solarviewer.utils.AppLogger
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module
class DataModule {

    @Provides
    @Singleton
    fun providesLogger(@Named("isDebug") isDebug: Boolean): Logger = AppLogger(isDebug)

    @Provides
    @Singleton
    fun providesPreferenceStore(context: Context): PreferenceStore = PreferenceStore(context)

    @Provides
    @Singleton
    fun providesEnphaseComponent(
        @Named("isDebug") isDebug: Boolean,
        preferenceStore: PreferenceStore
    ): EnphaseComponent =
        EnphaseComponent.builder()
            .apiKey(BuildConfig.API_KEY)
            .clientId(BuildConfig.CLIENT_ID)
            .clientSecret(BuildConfig.CLIENT_SECRET)
            .tokenStore(preferenceStore)
            .isDebug(isDebug)
            .build()

    @Provides
    @Singleton
    fun providesTokenStore(preferenceStore: PreferenceStore): TokenStore = preferenceStore

    @Provides
    @Singleton
    fun providesAuthApi(component: EnphaseComponent): AuthApi = component.authApi()

    @Provides
    @Singleton
    fun providesSolarRepository(
        context: Context,
        component: EnphaseComponent,
        logger: Logger,
        clock: Clock
    ): SolarRepository = EnphaseSolarRepository(context, clock, component.api(), logger)

    @Provides
    @Singleton
    fun providesClock(): Clock = AppClock()

    @Provides
    @Singleton
    fun providesLocalSettings(preferenceStore: PreferenceStore): LocalSettings = preferenceStore
}
