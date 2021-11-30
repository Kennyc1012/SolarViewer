package com.kennyc.solarviewer.di.components

import android.content.Context
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.ExperimentalComposeUiApi
import com.kennyc.solarviewer.MainActivity
import com.kennyc.solarviewer.MainActivityCompose
import com.kennyc.solarviewer.data.Logger
import com.kennyc.solarviewer.di.modules.DataModule
import com.kennyc.solarviewer.di.modules.ViewModelFactoryModule
import dagger.BindsInstance
import dagger.Component
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Component(modules = [DataModule::class, ViewModelFactoryModule::class])
interface AppComponent {

    fun fragmentComponentBuilder(): FragmentComponent.Builder

    fun logger(): Logger

    fun inject(activity: MainActivity)

    @ExperimentalMaterial3Api
    @ExperimentalComposeUiApi
    fun inject(activity: MainActivityCompose)

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun appContext(context: Context): Builder

        @BindsInstance
        fun isDebug(@Named("isDebug") isDebug: Boolean): Builder

        fun build(): AppComponent
    }
}