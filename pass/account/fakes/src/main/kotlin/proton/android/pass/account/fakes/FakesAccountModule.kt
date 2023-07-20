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
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.key.domain.repository.PublicAddressRepository
import me.proton.core.payment.domain.PaymentManager
import me.proton.core.usersettings.domain.repository.DeviceSettingsRepository
import proton.android.pass.account.api.AccountOrchestrators

@Module
@InstallIn(SingletonComponent::class)
abstract class FakesAccountModule {

    @Binds
    abstract fun bindAccountOrchestrators(
        impl: TestAccountOrchestrators
    ): AccountOrchestrators

    @Binds
    abstract fun bindAccountManager(
        impl: TestAccountManager
    ): AccountManager

    @Binds
    abstract fun bindPaymentManager(
        impl: TestPaymentManager
    ): PaymentManager

    @Binds
    abstract fun bindDeviceSettingsRepository(
        impl: TestDeviceSettingsRepository
    ): DeviceSettingsRepository

    @Binds
    abstract fun bindPublicAddressRepository(
        impl: TestPublicAddressRepository
    ): PublicAddressRepository
}
