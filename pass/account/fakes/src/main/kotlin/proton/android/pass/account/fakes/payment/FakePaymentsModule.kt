/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.account.fakes.payment

import android.app.Activity
import dagger.Binds
import dagger.BindsOptionalOf
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import me.proton.core.country.domain.entity.Country
import me.proton.core.country.domain.repository.CountriesRepository
import me.proton.core.domain.entity.AppStore
import me.proton.core.domain.entity.UserId
import me.proton.core.humanverification.domain.HumanVerificationManager
import me.proton.core.network.domain.client.ClientId
import me.proton.core.network.domain.client.ClientIdProvider
import me.proton.core.network.domain.humanverification.HumanVerificationAvailableMethods
import me.proton.core.network.domain.humanverification.HumanVerificationDetails
import me.proton.core.network.domain.humanverification.HumanVerificationListener
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionListener
import me.proton.core.payment.domain.entity.GooglePurchaseToken
import me.proton.core.payment.domain.entity.ProtonPaymentToken
import me.proton.core.payment.domain.features.IsMobileUpgradesEnabled
import me.proton.core.payment.domain.repository.GooglePurchaseRepository
import me.proton.core.payment.domain.usecase.AcknowledgeGooglePlayPurchase
import me.proton.core.payment.domain.usecase.ConvertToObservabilityGiapStatus
import me.proton.core.payment.domain.usecase.FindUnacknowledgedGooglePurchase
import me.proton.core.payment.domain.usecase.GetStorePrice
import me.proton.core.payment.domain.usecase.GoogleServicesUtils
import me.proton.core.payment.domain.usecase.LaunchGiapBillingFlow
import me.proton.core.payment.domain.usecase.PaymentProvider
import me.proton.core.payment.domain.usecase.PrepareGiapPurchase
import me.proton.core.payment.domain.usecase.ProtonIAPBillingLibrary
import me.proton.core.payment.presentation.ActivePaymentProvider
import me.proton.core.payment.presentation.entity.SecureEndpoint
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface FakePaymentsModule {

    /** Optional binding, provided by payment-iap-dagger. */
    @BindsOptionalOf
    fun optionalAcknowledgeGooglePlayPurchase(): AcknowledgeGooglePlayPurchase

    /** Optional binding, provided by payment-iap-dagger. */
    @BindsOptionalOf
    fun optionalFindUnredeemedGooglePurchase(): FindUnacknowledgedGooglePurchase

    /** Optional binding, provided by payment-iap-dagger. */
    @BindsOptionalOf
    fun optionalGetPlanAndCurrency(): GetStorePrice

    @BindsOptionalOf
    fun optionalLaunchGiapBillingFlow(): LaunchGiapBillingFlow<Activity>

    @BindsOptionalOf
    fun optionalPrepareGiapPurchase(): PrepareGiapPurchase

    @BindsOptionalOf
    fun optionalConvertToObservabilityGiapStatus(): ConvertToObservabilityGiapStatus

    @BindsOptionalOf
    fun bindGoogleServicesUtils(): GoogleServicesUtils

    @Binds
    fun bindIsMobileUpgradesEnabled(impl: FakeIsMobileUpgradesEnabled): IsMobileUpgradesEnabled

    @Binds
    fun bindProtonIAPBillingLibrary(impl: FakeProtonIAPBillingLibrary): ProtonIAPBillingLibrary

    @Binds
    fun bindCountriesRepository(impl: FakeCountriesRepository): CountriesRepository

    @Binds
    fun bindGooglePurchaseRepository(impl: FakeGooglePurchaseRepository): GooglePurchaseRepository

    @Binds
    fun bindClientIdProvider(impl: FakeClientIdProvider): ClientIdProvider

    @Binds
    fun bindHumanVerificationManager(impl: FakeHumanVerificationManager): HumanVerificationManager

    @Binds
    fun bindActivePaymentProvider(impl: FakeActivePaymentProvider): ActivePaymentProvider

    @Binds
    fun bindSessionListener(impl: FakeSessionListener): SessionListener
}

@Module
@InstallIn(SingletonComponent::class)
object FakePaymentsProvideModule {
    @[Provides Singleton]
    fun provideFakeSecureEndpoint() = SecureEndpoint("foo")

    @[Provides Singleton]
    fun provideAppStore() = AppStore.GooglePlay
}

@Singleton
class FakeIsMobileUpgradesEnabled @Inject constructor() : IsMobileUpgradesEnabled {
    override fun invoke(userId: UserId?): Boolean = false

    override fun isLocalEnabled(): Boolean = false

    override fun isRemoteEnabled(userId: UserId?): Boolean = false
}

@Singleton
class FakeProtonIAPBillingLibrary @Inject constructor() : ProtonIAPBillingLibrary {
    override fun isAvailable(): Boolean = false
}

@Singleton
class FakeCountriesRepository @Inject constructor() : CountriesRepository {
    override suspend fun getAllCountriesSorted(): List<Country> = emptyList()
    override suspend fun getCountry(countryName: String): Country? = null
}

@Singleton
class FakeGooglePurchaseRepository @Inject constructor() : GooglePurchaseRepository {
    override suspend fun deleteByGooglePurchaseToken(googlePurchaseToken: GooglePurchaseToken) {
    }

    override suspend fun deleteByProtonPaymentToken(paymentToken: ProtonPaymentToken) {
    }

    override suspend fun findGooglePurchaseToken(paymentToken: ProtonPaymentToken): GooglePurchaseToken? = null

    override suspend fun updateGooglePurchase(
        googlePurchaseToken: GooglePurchaseToken,
        paymentToken: ProtonPaymentToken
    ) {
    }
}

@Singleton
class FakeClientIdProvider @Inject constructor() : ClientIdProvider {
    override suspend fun getClientId(sessionId: SessionId?): ClientId? = null
}

@Singleton
class FakeHumanVerificationManager @Inject constructor() : HumanVerificationManager {
    override fun onHumanVerificationStateChanged(initialState: Boolean): Flow<HumanVerificationDetails> = flowOf()

    override suspend fun addDetails(details: HumanVerificationDetails) {
    }

    override suspend fun clearDetails(clientId: ClientId) {
    }

    override suspend fun getHumanVerificationDetails(clientId: ClientId): HumanVerificationDetails? = null

    override suspend fun onHumanVerificationNeeded(
        clientId: ClientId,
        methods: HumanVerificationAvailableMethods
    ): HumanVerificationListener.HumanVerificationResult = HumanVerificationListener.HumanVerificationResult.Success

    override suspend fun onHumanVerificationInvalid(clientId: ClientId) {
    }
}

@Singleton
class FakeActivePaymentProvider @Inject constructor() : ActivePaymentProvider {
    override suspend fun getActivePaymentProvider(): PaymentProvider? = null

    override fun switchNextPaymentProvider(): PaymentProvider? = null

    override fun getNextPaymentProviderText(): Int? = null
}


@Singleton
class FakeSessionListener @Inject constructor() : SessionListener {
    override suspend fun <T> withLock(sessionId: SessionId?, action: suspend () -> T): T = action()

    override suspend fun requestSession(): Boolean = false

    override suspend fun refreshSession(session: Session): Boolean = false

    override suspend fun onSessionTokenCreated(userId: UserId?, session: Session) {
    }

    override suspend fun onSessionTokenRefreshed(session: Session) {
    }

    override suspend fun onSessionScopesRefreshed(sessionId: SessionId, scopes: List<String>) {
    }

    override suspend fun onSessionForceLogout(session: Session, httpCode: Int) {
    }
}

