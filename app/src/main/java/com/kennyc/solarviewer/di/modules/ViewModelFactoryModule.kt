package com.kennyc.solarviewer.di.modules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kennyc.solarviewer.SystemsViewModel
import com.kennyc.solarviewer.daily.DailyViewModel
import com.kennyc.solarviewer.di.DaggerViewModelFactory
import com.kennyc.solarviewer.di.ViewModelKey
import com.kennyc.solarviewer.home.HomeViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelFactoryModule {

    @Binds
    abstract fun bindViewModelFactory(viewModelFactory: DaggerViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(HomeViewModel::class)
    abstract fun bindCreateHomeModel(viewModel: HomeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DailyViewModel::class)
    abstract fun bindCreateDailyModel(viewModel: DailyViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SystemsViewModel::class)
    abstract fun bindSystemsViewModel(viewModel: SystemsViewModel): ViewModel
}