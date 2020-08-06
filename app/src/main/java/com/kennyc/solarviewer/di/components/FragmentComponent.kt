package com.kennyc.solarviewer.di.components

import androidx.fragment.app.Fragment
import com.kennyc.solarviewer.daily.DailyFragment
import com.kennyc.solarviewer.di.modules.ViewModelFactoryModule
import com.kennyc.solarviewer.home.HomeFragment
import dagger.BindsInstance
import dagger.Subcomponent

@Subcomponent(modules = [ViewModelFactoryModule::class])
interface FragmentComponent {

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun fragment(fragment: Fragment): Builder

        fun build(): FragmentComponent
    }

    fun inject(fragment: HomeFragment)

    fun inject(fragment: DailyFragment)
}