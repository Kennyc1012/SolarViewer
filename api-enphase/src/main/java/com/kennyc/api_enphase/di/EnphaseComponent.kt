package com.kennyc.api_enphase.di

import com.kennyc.api_enphase.EnphaseApi
import com.kennyc.api_enphase.auth.AuthApi
import com.kennyc.api_enphase.auth.TokenStore
import dagger.BindsInstance
import dagger.Component
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Component(modules = [EnphaseModule::class])
interface EnphaseComponent {

    fun api(): EnphaseApi

    fun authApi(): AuthApi

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun apiKey(@Named("apiKey") apiKey: String): Builder

        @BindsInstance
        fun clientId(@Named("clientId") clientId: String): Builder

        @BindsInstance
        fun clientSecret(@Named("clientSecret") clientSecret: String): Builder

        @BindsInstance
        fun tokenStore(tokenStore: TokenStore): Builder

        @BindsInstance
        fun isDebug(@Named("isDebug") isDebug: Boolean): Builder

        fun build(): EnphaseComponent
    }

    companion object {
        fun builder(): Builder = DaggerEnphaseComponent.builder()
    }
}
