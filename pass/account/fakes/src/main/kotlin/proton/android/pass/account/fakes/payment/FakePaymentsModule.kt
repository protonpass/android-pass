/*
 * Copyright (c) 2026 Proton AG
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
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.country.domain.repository.CountriesRepository
import me.proton.core.humanverification.domain.HumanVerificationManager
import me.proton.core.network.domain.client.ClientIdProvider
import me.proton.core.network.domain.session.SessionListener
import me.proton.core.plan.domain.usecase.PerformGiapPurchase
import me.proton.core.payment.domain.features.IsMobileUpgradesEnabled
import me.proton.core.payment.domain.repository.GooglePurchaseRepository
import me.proton.core.payment.domain.usecase.AcknowledgeGooglePlayPurchase
import me.proton.core.payment.domain.usecase.ConvertToObservabilityGiapStatus
import me.proton.core.payment.domain.usecase.FindUnacknowledgedGooglePurchase
import me.proton.core.payment.domain.usecase.GetStorePrice
import me.proton.core.payment.domain.usecase.GoogleServicesUtils
import me.proton.core.payment.domain.usecase.LaunchGiapBillingFlow
import me.proton.core.payment.domain.usecase.PrepareGiapPurchase
import me.proton.core.payment.domain.usecase.ProtonIAPBillingLibrary
import me.proton.core.payment.presentation.ActivePaymentProvider

@Module
@InstallIn(SingletonComponent::class)
interface FakePaymentsModule {

    @BindsOptionalOf
    fun optionalAcknowledgeGooglePlayPurchase(): AcknowledgeGooglePlayPurchase

    @BindsOptionalOf
    fun optionalFindUnredeemedGooglePurchase(): FindUnacknowledgedGooglePurchase

    @BindsOptionalOf
    fun optionalGetPlanAndCurrency(): GetStorePrice

    @BindsOptionalOf
    fun optionalLaunchGiapBillingFlow(): LaunchGiapBillingFlow<Activity>

    @BindsOptionalOf
    fun optionalPrepareGiapPurchase(): PrepareGiapPurchase

    @BindsOptionalOf
    fun optionalPerformGiapPurchase(): PerformGiapPurchase<Activity>

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
