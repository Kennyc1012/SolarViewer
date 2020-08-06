package com.kennyc.api_enphase.di

import com.kennyc.api_enphase.EnphaseApi
import dagger.BindsInstance
import dagger.Component
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Component(modules = [EnphaseModule::class])
interface EnphaseComponent {

    fun api(): EnphaseApi

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun appId(@Named("appId") appId: String): Builder

        @BindsInstance
        fun userId(@Named("userId") userId: String): Builder

        @BindsInstance
        fun isDebug(@Named("isDebug") isDebug: Boolean): Builder

        fun build(): EnphaseComponent
    }

    companion object {
        fun builder(): Builder = DaggerEnphaseComponent.builder()
    }
}