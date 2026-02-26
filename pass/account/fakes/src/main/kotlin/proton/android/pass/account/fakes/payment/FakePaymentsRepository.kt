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

import me.proton.core.domain.entity.AppStore
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.payment.domain.entity.PaymentMethod
import me.proton.core.payment.domain.entity.PaymentStatus
import me.proton.core.payment.domain.entity.PaymentTokenResult
import me.proton.core.payment.domain.entity.PaymentType
import me.proton.core.payment.domain.entity.ProtonPaymentToken
import me.proton.core.payment.domain.repository.PaymentsRepository
import javax.inject.Inject

@Suppress("NotImplementedDeclaration")
class FakePaymentsRepository @Inject constructor() : PaymentsRepository {
    override suspend fun createOmnichannelPaymentToken(
        sessionUserId: SessionUserId?,
        packageName: String,
        productId: String,
        orderId: String
    ): PaymentTokenResult.CreatePaymentTokenResult {
        TODO("Not yet implemented")
    }

    override suspend fun createPaymentToken(
        sessionUserId: SessionUserId?,
        paymentType: PaymentType
    ): PaymentTokenResult.CreatePaymentTokenResult {
        TODO("Not yet implemented")
    }

    override suspend fun getPaymentTokenStatus(
        sessionUserId: SessionUserId?,
        paymentToken: ProtonPaymentToken
    ): PaymentTokenResult.PaymentTokenStatusResult {
        TODO("Not yet implemented")
    }

    override suspend fun getAvailablePaymentMethods(sessionUserId: SessionUserId): List<PaymentMethod> {
        TODO("Not yet implemented")
    }

    override suspend fun getPaymentStatus(sessionUserId: SessionUserId?, appStore: AppStore): PaymentStatus {
        TODO("Not yet implemented")
    }

}
