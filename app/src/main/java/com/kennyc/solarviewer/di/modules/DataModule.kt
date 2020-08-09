package com.kennyc.solarviewer.di.modules

import android.content.Context
import androidx.annotation.ColorInt
import com.kennyc.api_enphase.di.EnphaseComponent
import com.kennyc.data_enphase.EnphaseSolarRepository
import com.kennyc.solarviewer.BuildConfig
import com.kennyc.solarviewer.R
import com.kennyc.solarviewer.data.Clock
import com.kennyc.solarviewer.data.LocalSettings
import com.kennyc.solarviewer.data.Logger
import com.kennyc.solarviewer.data.SolarRepository
import com.kennyc.solarviewer.data.model.CoroutineDispatchProvider
import com.kennyc.solarviewer.utils.AppClock
import com.kennyc.solarviewer.utils.AppLocalSettings
import com.kennyc.solarviewer.utils.AppLogger
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.Dispatchers
import javax.inject.Named
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Module
class DataModule {

    @Provides
    @Singleton
    fun providesLogger(@Named("isDebug") isDebug: Boolean): Logger = AppLogger(isDebug)

    @Provides
    @Singleton
    fun providesEnphaseComponent(@Named("isDebug") isDebug: Boolean): EnphaseComponent =
        EnphaseComponent.builder()
            .appId(BuildConfig.APP_ID)
            .userId(BuildConfig.USER_ID)
            .isDebug(isDebug)
            .build()

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
    fun providesCoroutineDispatchProvider(): CoroutineDispatchProvider {
        return object : CoroutineDispatchProvider {
            override val main: CoroutineContext get() = Dispatchers.Main
            override val default: CoroutineContext get() = Dispatchers.Default
            override val io: CoroutineContext get() = Dispatchers.IO
            override val unconfined: CoroutineContext get() = Dispatchers.Unconfined
        }
    }

    @Provides
    @Singleton
    fun providesClock(): Clock = AppClock()

    @Provides
    @Singleton
    fun providesLocalSettings(context: Context): LocalSettings = AppLocalSettings(context)

    @Provides
    @Singleton
    @ColorInt
    @Named("produced-color")
    fun providesProducedColor(context: Context): Int =
        context.resources.getColor(R.color.color_production, context.theme)

    @Provides
    @Singleton
    @ColorInt
    @Named("consumed-color")
    fun providesConsumedColor(context: Context): Int =
        context.resources.getColor(R.color.color_consumption, context.theme)
}