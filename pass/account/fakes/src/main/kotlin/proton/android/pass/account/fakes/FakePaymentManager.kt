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

import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.PaymentManager
import me.proton.core.payment.domain.usecase.PaymentProvider
import javax.inject.Inject

class FakePaymentManager @Inject constructor() : PaymentManager {

    var paymentProviders: Set<PaymentProvider> = emptySet()
    var isUpgradeAvailable: Boolean = true
    var isSubscriptionAvailable: Boolean = true

    override suspend fun getPaymentProviders(userId: UserId?, refresh: Boolean): Set<PaymentProvider> = paymentProviders

    override suspend fun isUpgradeAvailable(userId: UserId?, refresh: Boolean): Boolean = isUpgradeAvailable

    override suspend fun isSubscriptionAvailable(userId: UserId, refresh: Boolean): Boolean = isSubscriptionAvailable
}
