/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton Technologies AG and Proton Mail.
 *
 * Proton Mail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Mail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Mail. If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.android.pass.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.android.pass.BuildConfig
import me.proton.android.pass.network.PassApiClient
import me.proton.core.network.data.client.ExtraHeaderProviderImpl
import me.proton.core.network.data.di.AlternativeApiPins
import me.proton.core.network.data.di.BaseProtonApiUrl
import me.proton.core.network.data.di.CertificatePins
import me.proton.core.network.data.di.Constants
import me.proton.core.network.data.di.DohProviderUrls
import me.proton.core.network.domain.ApiClient
import me.proton.core.network.domain.client.ExtraHeaderProvider
import me.proton.core.network.domain.serverconnection.DohAlternativesListener
import me.proton.core.pass.data.local.LocalItemDataSource
import me.proton.core.pass.data.local.LocalItemDataSourceImpl
import me.proton.core.pass.data.local.LocalShareDataSource
import me.proton.core.pass.data.local.LocalShareDataSourceImpl
import me.proton.core.pass.data.local.LocalVaultItemKeyDataSource
import me.proton.core.pass.data.local.LocalVaultItemKeyDataSourceImpl
import me.proton.core.pass.data.remote.RemoteAliasDataSource
import me.proton.core.pass.data.remote.RemoteAliasDataSourceImpl
import me.proton.core.pass.data.remote.RemoteItemDataSource
import me.proton.core.pass.data.remote.RemoteItemDataSourceImpl
import me.proton.core.pass.data.remote.RemoteKeyPacketDataSource
import me.proton.core.pass.data.remote.RemoteKeyPacketDataSourceImpl
import me.proton.core.pass.data.remote.RemoteShareDataSource
import me.proton.core.pass.data.remote.RemoteShareDataSourceImpl
import me.proton.core.pass.data.remote.RemoteVaultItemKeyDataSource
import me.proton.core.pass.data.remote.RemoteVaultItemKeyDataSourceImpl
import me.proton.core.pass.data.repositories.AliasRepositoryImpl
import me.proton.core.pass.data.repositories.ItemRepositoryImpl
import me.proton.core.pass.data.repositories.KeyPacketRepositoryImpl
import me.proton.core.pass.data.repositories.ShareRepositoryImpl
import me.proton.core.pass.data.repositories.VaultKeyRepositoryImpl
import me.proton.core.pass.domain.repositories.AliasRepository
import me.proton.core.pass.domain.repositories.ItemRepository
import me.proton.core.pass.domain.repositories.KeyPacketRepository
import me.proton.core.pass.domain.repositories.ShareRepository
import me.proton.core.pass.domain.repositories.VaultKeyRepository
import me.proton.core.util.kotlin.takeIfNotBlank
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
@Suppress("LongParameterList")
object NetworkModule {

    const val HOST = BuildConfig.HOST
    const val API_HOST = "api.$HOST"
    const val BASE_URL = "https://$API_HOST"

    @Provides
    @BaseProtonApiUrl
    fun provideProtonApiUrl(): HttpUrl = BASE_URL.toHttpUrl()

    @Provides
    @Singleton
    fun provideExtraHeaderProvider(): ExtraHeaderProvider = ExtraHeaderProviderImpl().apply {
        val proxyToken: String? = BuildConfig.PROXY_TOKEN
        proxyToken?.takeIfNotBlank()?.let { addHeaders("X-atlas-secret" to it) }
    }

    @DohProviderUrls
    @Provides
    fun provideDohProviderUrls(): Array<String> = Constants.DOH_PROVIDERS_URLS

    @CertificatePins
    @Provides
    fun provideCertificatePins(): Array<String> =
        Constants.DEFAULT_SPKI_PINS.takeIf { BuildConfig.USE_DEFAULT_PINS } ?: emptyArray()

    @AlternativeApiPins
    @Provides
    fun provideAlternativeApiPins(): List<String> =
        Constants.ALTERNATIVE_API_SPKI_PINS.takeIf { BuildConfig.USE_DEFAULT_PINS } ?: emptyList()

    @Provides
    @Singleton
    fun provideDohAlternativesListener(): DohAlternativesListener? = null
}

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkBindModule {
    @Binds
    abstract fun bindApiClient(apiClient: PassApiClient): ApiClient

    @Binds
    abstract fun bindRemoteShareDataSource(
        remoteShareDataSource: RemoteShareDataSourceImpl
    ): RemoteShareDataSource

    @Binds
    abstract fun bindLocalShareDataSource(
        localShareDataSource: LocalShareDataSourceImpl
    ): LocalShareDataSource

    @Binds
    abstract fun bindShareRepository(
        shareRepositoryImpl: ShareRepositoryImpl
    ): ShareRepository

    @Binds
    abstract fun bindVaultKeyRepository(
        vaultKeyRepositoryImpl: VaultKeyRepositoryImpl
    ): VaultKeyRepository

    @Binds
    abstract fun bindRemoteVaultKeyDataSource(
        remoteVaultItemKeyDataSourceImpl: RemoteVaultItemKeyDataSourceImpl
    ): RemoteVaultItemKeyDataSource

    @Binds
    abstract fun bindLocalVaultKeyDataSource(
        localVaultItemKeyDataSourceImpl: LocalVaultItemKeyDataSourceImpl
    ): LocalVaultItemKeyDataSource

    @Binds
    abstract fun bindItemRepository(itemRepositoryImpl: ItemRepositoryImpl): ItemRepository

    @Binds
    abstract fun bindRemoteItemDataSource(
        remoteItemDataSourceImpl: RemoteItemDataSourceImpl
    ): RemoteItemDataSource

    @Binds
    abstract fun bindLocalItemDataSource(
        localItemDataSourceImpl: LocalItemDataSourceImpl
    ): LocalItemDataSource

    @Binds
    abstract fun bindKeyPacketRepository(
        keyPacketRepository: KeyPacketRepositoryImpl
    ): KeyPacketRepository

    @Binds
    abstract fun bindRemoteKeyPacketDataSource(
        remoteKeyPacketDataSource: RemoteKeyPacketDataSourceImpl
    ): RemoteKeyPacketDataSource

    @Binds
    abstract fun bindAliasRepository(
        aliasRepository: AliasRepositoryImpl
    ): AliasRepository

    @Binds
    abstract fun bindRemoteAliasDataSource(
        remoteAliasDataSource: RemoteAliasDataSourceImpl
    ): RemoteAliasDataSource
}
