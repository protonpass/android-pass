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

package proton.android.pass.account.fakes

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.account.domain.repository.AccountRepository
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.AccountWorkflowHandler
import me.proton.core.accountmanager.domain.SessionManager
import me.proton.core.auth.domain.feature.IsFido2Enabled
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.auth.domain.usecase.PostLoginAccountSetup
import me.proton.core.devicemigration.domain.feature.IsEasyDeviceMigrationEnabled
import me.proton.core.devicemigration.domain.usecase.GenerateEdmCode
import me.proton.core.devicemigration.domain.usecase.IsEasyDeviceMigrationAvailable
import me.proton.core.devicemigration.domain.usecase.ObserveEdmCode
import me.proton.core.domain.arch.ErrorMessageContext
import me.proton.core.domain.entity.Product
import me.proton.core.featureflag.domain.repository.FeatureFlagRepository
import me.proton.core.key.domain.repository.PublicAddressRepository
import me.proton.core.network.domain.ApiClient
import me.proton.core.network.domain.NetworkManager
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.observability.domain.ObservabilityRepository
import me.proton.core.observability.domain.ObservabilityWorkerManager
import me.proton.core.observability.domain.usecase.IsObservabilityEnabled
import me.proton.core.observability.domain.usecase.SendObservabilityEvents
import me.proton.core.payment.domain.PaymentManager
import me.proton.core.payment.domain.features.IsOmnichannelEnabled
import me.proton.core.payment.domain.repository.PaymentsRepository
import me.proton.core.payment.domain.repository.PurchaseRepository
import me.proton.core.payment.domain.usecase.CreatePaymentTokenForGooglePurchase
import me.proton.core.payment.domain.usecase.FindGooglePurchaseForPaymentOrderId
import me.proton.core.plan.domain.repository.PlansRepository
import me.proton.core.plan.domain.usecase.PerformSubscribe
import me.proton.core.user.domain.UserAddressManager
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.repository.DomainRepository
import me.proton.core.user.domain.repository.PassphraseRepository
import me.proton.core.user.domain.repository.UserAddressRepository
import me.proton.core.user.domain.repository.UserRepository
import me.proton.core.usersettings.domain.repository.DeviceSettingsRepository
import me.proton.core.usersettings.domain.repository.UserSettingsRepository
import me.proton.core.usersettings.domain.usecase.ObserveRegisteredSecurityKeys
import me.proton.core.usersettings.domain.usecase.ObserveUserSettings
import me.proton.core.util.kotlin.CoroutineScopeProvider
import me.proton.core.util.kotlin.DispatcherProvider
import proton.android.pass.account.api.AccountOrchestrators
import proton.android.pass.account.fakes.accountmanager.FakeAccountWorkflowHandler
import proton.android.pass.account.fakes.accountmanager.FakeSessionManager
import proton.android.pass.account.fakes.auth.FakeAuthRepository
import proton.android.pass.account.fakes.auth.FakeUserCheck
import proton.android.pass.account.fakes.crypto.FakePassphraseRepository
import proton.android.pass.account.fakes.devicemigration.FakeIsEasyDeviceMigrationAvailable
import proton.android.pass.account.fakes.devicemigration.FakeIsEasyDeviceMigrationEnabled
import proton.android.pass.account.fakes.domain.FakeErrorMessageContext
import proton.android.pass.account.fakes.network.FakeApiClient
import proton.android.pass.account.fakes.network.FakeNetworkManager
import proton.android.pass.account.fakes.network.FakeSessionProvider
import proton.android.pass.account.fakes.observability.FakeIsObservabilityEnabled
import proton.android.pass.account.fakes.observability.FakeObservabilityRepository
import proton.android.pass.account.fakes.observability.FakeObservabilityWorkerManager
import proton.android.pass.account.fakes.observability.FakeSendObservabilityEvents
import proton.android.pass.account.fakes.payment.FakeCreatePaymentTokenForGooglePurchase
import proton.android.pass.account.fakes.payment.FakeFindGooglePurchaseForPaymentOrderId
import proton.android.pass.account.fakes.payment.FakeIsOmnichannelEnabled
import proton.android.pass.account.fakes.payment.FakePaymentsRepository
import proton.android.pass.account.fakes.payment.FakePurchaseRepository
import proton.android.pass.account.fakes.plan.FakePerformSubscribe
import proton.android.pass.account.fakes.plan.FakePlansRepository
import proton.android.pass.account.fakes.user.FakeDomainRepository
import proton.android.pass.account.fakes.user.FakeUserAddressManager
import java.util.Optional

@Module
@InstallIn(SingletonComponent::class)
@Suppress("TooManyFunctions")
abstract class FakesAccountModule {

    @Binds
    abstract fun bindAccountOrchestrators(impl: TestAccountOrchestrators): AccountOrchestrators

    @Binds
    abstract fun bindAccountManager(impl: TestAccountManager): AccountManager

    @Binds
    abstract fun bindPaymentManager(impl: TestPaymentManager): PaymentManager

    @Binds
    abstract fun bindDeviceSettingsRepository(impl: TestDeviceSettingsRepository): DeviceSettingsRepository

    @Binds
    abstract fun bindPublicAddressRepository(impl: TestPublicAddressRepository): PublicAddressRepository

    @Binds
    abstract fun bindFeatureFlagRepository(impl: TestFeatureFlagRepository): FeatureFlagRepository

    @Binds
    abstract fun bindUserManager(impl: FakeUserManager): UserManager

    @Binds
    abstract fun bindUserSettingsRepository(impl: FakeUserSettingsRepository): UserSettingsRepository

    @Binds
    abstract fun bindAccountRepository(impl: FakeAccountRepository): AccountRepository

    @Binds
    abstract fun bindIsFido2Enabled(impl: FakeIsFido2Enabled): IsFido2Enabled

    @Binds
    abstract fun bindDispatcherProvider(impl: FakeDispatcherProvider): DispatcherProvider

    @Binds
    abstract fun bindCoroutineScopeProvider(impl: FakeCoroutineScopeProvider): CoroutineScopeProvider

    @Binds
    abstract fun bindIsObservabilityEnabled(impl: FakeIsObservabilityEnabled): IsObservabilityEnabled

    @Binds
    abstract fun bindObservabilityRepository(impl: FakeObservabilityRepository): ObservabilityRepository

    @Binds
    abstract fun bindObservabilityWorkerManager(impl: FakeObservabilityWorkerManager): ObservabilityWorkerManager

    @Binds
    abstract fun bindSendObservabilityEvents(impl: FakeSendObservabilityEvents): SendObservabilityEvents

    @Binds
    abstract fun bindErrorMessageContext(impl: FakeErrorMessageContext): ErrorMessageContext

    @Binds
    abstract fun bindAuthRepository(impl: FakeAuthRepository): AuthRepository

    @Binds
    abstract fun bindSessionProvider(impl: FakeSessionProvider): SessionProvider

    @Binds
    abstract fun bindPassphraseRepository(impl: FakePassphraseRepository): PassphraseRepository

    @Binds
    abstract fun bindIsEasyDeviceMigrationEnabled(impl: FakeIsEasyDeviceMigrationEnabled): IsEasyDeviceMigrationEnabled

    @Binds
    abstract fun bindIsEasyDeviceMigrationAvailable(
        impl: FakeIsEasyDeviceMigrationAvailable
    ): IsEasyDeviceMigrationAvailable

    @Binds
    abstract fun bindFakeApiClient(impl: FakeApiClient): ApiClient

    @Binds
    abstract fun bindFakeNetworkManager(impl: FakeNetworkManager): NetworkManager

    @Binds
    abstract fun bindAccountWorkflowHandler(impl: FakeAccountWorkflowHandler): AccountWorkflowHandler

    @Binds
    abstract fun bindPerformSubscribe(impl: FakePerformSubscribe): PerformSubscribe

    @Binds
    abstract fun bindPurchaseRepository(impl: FakePurchaseRepository): PurchaseRepository

    @Binds
    abstract fun bindPaymentsRepository(impl: FakePaymentsRepository): PaymentsRepository

    @Binds
    abstract fun bindPlansRepository(impl: FakePlansRepository): PlansRepository

    @Binds
    abstract fun bindUserCheck(impl: FakeUserCheck): PostLoginAccountSetup.UserCheck

    @Binds
    abstract fun bindSessionManager(impl: FakeSessionManager): SessionManager

    @Binds
    abstract fun bindUserRepository(impl: TestUserRepository): UserRepository

    @Binds
    abstract fun bindUserAddressRepository(impl: TestUserAddressRepository): UserAddressRepository

    @Binds
    abstract fun bindUserAddressManager(impl: FakeUserAddressManager): UserAddressManager

    @Binds
    abstract fun bindDomainRepository(impl: FakeDomainRepository): DomainRepository

    @Binds
    abstract fun bindCreatePaymentTokenForGooglePurchase(
        impl: FakeCreatePaymentTokenForGooglePurchase
    ): CreatePaymentTokenForGooglePurchase

    @Binds
    abstract fun bindIsOmnichannelEnabled(impl: FakeIsOmnichannelEnabled): IsOmnichannelEnabled

    @Binds
    abstract fun bindFindGooglePurchaseForPaymentOrderId(
        impl: FakeFindGooglePurchaseForPaymentOrderId
    ): FindGooglePurchaseForPaymentOrderId

    companion object {
        @Provides
        fun provideAccountType(): AccountType = AccountType.External

        @Provides
        fun provideProduct(): Product = Product.Pass

        @Provides
        fun provideObserveEdmCode(generateEdmCode: GenerateEdmCode): ObserveEdmCode = ObserveEdmCode(generateEdmCode)

        @Provides
        fun provideOptionalFindGooglePurchase(
            impl: FindGooglePurchaseForPaymentOrderId
        ): Optional<FindGooglePurchaseForPaymentOrderId> = Optional.of(impl)
    }
}

@Module
@InstallIn(SingletonComponent::class)
class FakesAccountModuleProvides {

    @Provides
    fun provideObserveRegisteredSecurityKeys(
        accountRepository: AccountRepository,
        isFido2Enabled: IsFido2Enabled,
        userSettingsRepository: FakeUserSettingsRepository
    ): ObserveRegisteredSecurityKeys = ObserveRegisteredSecurityKeys(
        accountRepository,
        isFido2Enabled,
        ObserveUserSettings(userSettingsRepository)
    )
}
