package me.proton.android.pass.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import me.proton.android.pass.BuildConfig
import me.proton.android.pass.network.PassApiClient
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.network.data.ApiManagerFactory
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.data.NetworkManager
import me.proton.core.network.data.NetworkPrefs
import me.proton.core.network.data.ProtonCookieStore
import me.proton.core.network.data.client.ClientIdProviderImpl
import me.proton.core.network.data.di.Constants
import me.proton.core.network.domain.ApiClient
import me.proton.core.network.domain.NetworkManager
import me.proton.core.network.domain.NetworkPrefs
import me.proton.core.network.domain.client.ClientIdProvider
import me.proton.core.network.domain.humanverification.HumanVerificationListener
import me.proton.core.network.domain.humanverification.HumanVerificationProvider
import me.proton.core.network.domain.server.ServerTimeListener
import me.proton.core.network.domain.session.SessionListener
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.util.kotlin.Logger
import java.io.File
import javax.inject.Singleton
import okhttp3.Cache

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideNetworkManager(@ApplicationContext context: Context): NetworkManager =
        NetworkManager(context)

    @Provides
    @Singleton
    fun provideNetworkPrefs(@ApplicationContext context: Context): NetworkPrefs =
        NetworkPrefs(context)

    @Provides
    @Singleton
    fun provideProtonCookieStore(@ApplicationContext context: Context): ProtonCookieStore =
        ProtonCookieStore(context)

    @Provides
    @Singleton
    fun provideClientIdProvider(protonCookieStore: ProtonCookieStore): ClientIdProvider =
        ClientIdProviderImpl(BuildConfig.BASE_URL, protonCookieStore)

    @Provides
    @Singleton
    fun provideServerTimeListener(context: CryptoContext) = object : ServerTimeListener {
        override fun onServerTimeUpdated(epochSeconds: Long) {
            context.pgpCrypto.updateTime(epochSeconds)
        }
    }

    @Provides
    @Singleton
    @Suppress("UNUSED_PARAMETER")
    fun provideApiFactory(
        @ApplicationContext context: Context,
        logger: Logger,
        apiClient: ApiClient,
        clientIdProvider: ClientIdProvider,
        serverTimeListener: ServerTimeListener,
        networkManager: NetworkManager,
        networkPrefs: NetworkPrefs,
        protonCookieStore: ProtonCookieStore,
        sessionProvider: SessionProvider,
        sessionListener: SessionListener,
        humanVerificationProvider: HumanVerificationProvider,
        humanVerificationListener: HumanVerificationListener
    ): ApiManagerFactory = ApiManagerFactory(
        BuildConfig.BASE_URL,
        apiClient,
        clientIdProvider,
        serverTimeListener,
        logger,
        networkManager,
        networkPrefs,
        sessionProvider,
        sessionListener,
        humanVerificationProvider,
        humanVerificationListener,
        protonCookieStore,
        CoroutineScope(Job() + Dispatchers.Default),
        certificatePins,
        alternativeApiPins,
        Cache(
            directory = File(context.cacheDir, "http_cache"),
            maxSize = 10L * 1024L * 1024L // 10 MiB
        )
    )

    @Provides
    @Singleton
    fun provideApiProvider(apiManagerFactory: ApiManagerFactory, sessionProvider: SessionProvider): ApiProvider =
        ApiProvider(apiManagerFactory, sessionProvider)

    private val certificatePins: Array<String> = when (BuildConfig.FLAVOR) {
        BuildConfig.FLAVOR_PRODUCTION -> Constants.DEFAULT_SPKI_PINS
        else -> emptyArray()
    }

    private val alternativeApiPins: List<String> = when (BuildConfig.FLAVOR) {
        BuildConfig.FLAVOR_PRODUCTION -> Constants.ALTERNATIVE_API_SPKI_PINS
        else -> emptyList()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkBindModule {

    @Binds
    abstract fun bindApiClient(apiClient: PassApiClient): ApiClient
}
