/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.network.fakes

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.proton.core.network.data.NetworkPrefsImpl
import me.proton.core.network.data.client.ExtraHeaderProviderImpl
import me.proton.core.network.data.di.AlternativeApiPins
import me.proton.core.network.data.di.BaseProtonApiUrl
import me.proton.core.network.data.di.CertificatePins
import me.proton.core.network.data.di.DohProviderUrls
import me.proton.core.network.domain.NetworkPrefs
import me.proton.core.network.domain.client.ExtraHeaderProvider
import me.proton.core.network.domain.serverconnection.DohAlternativesListener
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FakesNetworkModule {

    @Provides
    @BaseProtonApiUrl
    fun provideProtonApiUrl(): HttpUrl = "https://api.proton.me".toHttpUrl()

    @Provides
    @Singleton
    fun provideExtraHeaderProvider(): ExtraHeaderProvider =
        ExtraHeaderProviderImpl()

    @Provides
    @Singleton
    fun provideNetworkPrefs(@ApplicationContext context: Context): NetworkPrefs =
        NetworkPrefsImpl(context)

    @DohProviderUrls
    @Provides
    fun provideDohProviderUrls(): Array<String> = emptyArray()

    @CertificatePins
    @Provides
    fun provideCertificatePins(): Array<String> = emptyArray()

    @AlternativeApiPins
    @Provides
    fun provideAlternativeApiPins(): List<String> = emptyList()

    @Provides
    @Singleton
    fun provideDohAlternativesListener(): DohAlternativesListener? = null
}
