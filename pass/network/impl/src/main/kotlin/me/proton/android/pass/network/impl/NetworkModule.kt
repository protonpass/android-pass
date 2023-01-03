package me.proton.android.pass.network.impl

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.android.pass.appconfig.api.AppConfig
import me.proton.android.pass.network.api.NetworkMonitor
import me.proton.core.network.data.client.ExtraHeaderProviderImpl
import me.proton.core.network.data.di.AlternativeApiPins
import me.proton.core.network.data.di.BaseProtonApiUrl
import me.proton.core.network.data.di.CertificatePins
import me.proton.core.network.data.di.Constants
import me.proton.core.network.data.di.DohProviderUrls
import me.proton.core.network.domain.ApiClient
import me.proton.core.network.domain.client.ExtraHeaderProvider
import me.proton.core.network.domain.serverconnection.DohAlternativesListener
import me.proton.core.util.kotlin.takeIfNotBlank
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
@Suppress("LongParameterList")
object NetworkModule {

    @Provides
    @BaseProtonApiUrl
    fun provideProtonApiUrl(appConfig: AppConfig): HttpUrl =
        "https://api.${appConfig.host}".toHttpUrl()

    @Provides
    @Singleton
    fun provideExtraHeaderProvider(appConfig: AppConfig): ExtraHeaderProvider =
        ExtraHeaderProviderImpl().apply {
            appConfig.proxyToken
                ?.takeIfNotBlank()
                ?.let { addHeaders("X-atlas-secret" to it) }
        }

    @DohProviderUrls
    @Provides
    fun provideDohProviderUrls(): Array<String> = Constants.DOH_PROVIDERS_URLS

    @CertificatePins
    @Provides
    fun provideCertificatePins(appConfig: AppConfig): Array<String> =
        Constants.DEFAULT_SPKI_PINS.takeIf { appConfig.useDefaultPins } ?: emptyArray()

    @AlternativeApiPins
    @Provides
    fun provideAlternativeApiPins(appConfig: AppConfig): List<String> =
        Constants.ALTERNATIVE_API_SPKI_PINS.takeIf { appConfig.useDefaultPins } ?: emptyList()

    @Provides
    @Singleton
    fun provideDohAlternativesListener(): DohAlternativesListener? = null
}

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkBindModule {

    @Binds
    abstract fun bindNetworkMonitor(impl: NetworkMonitorImpl): NetworkMonitor

    @Binds
    abstract fun bindApiClient(apiClient: PassApiClient): ApiClient
}
