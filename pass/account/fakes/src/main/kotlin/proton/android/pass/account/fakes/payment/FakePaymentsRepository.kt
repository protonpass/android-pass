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
import me.proton.core.payment.domain.entity.PaymentTokenStatus
import me.proton.core.payment.domain.entity.PaymentType
import me.proton.core.payment.domain.entity.ProtonPaymentToken
import me.proton.core.payment.domain.repository.PaymentsRepository
import javax.inject.Inject

class FakePaymentsRepository @Inject constructor() : PaymentsRepository {

    var createOmnichannelPaymentTokenResult: PaymentTokenResult.CreatePaymentTokenResult =
        PaymentTokenResult.CreatePaymentTokenResult(
            status = PaymentTokenStatus.CHARGEABLE,
            approvalUrl = null,
            token = ProtonPaymentToken("fake-token"),
            returnHost = null
        )

    var createPaymentTokenResult: PaymentTokenResult.CreatePaymentTokenResult =
        PaymentTokenResult.CreatePaymentTokenResult(
            status = PaymentTokenStatus.CHARGEABLE,
            approvalUrl = null,
            token = ProtonPaymentToken("fake-token"),
            returnHost = null
        )

    var getPaymentTokenStatusResult: PaymentTokenResult.PaymentTokenStatusResult =
        PaymentTokenResult.PaymentTokenStatusResult(status = PaymentTokenStatus.CHARGEABLE)

    var getAvailablePaymentMethodsResult: List<PaymentMethod> = emptyList()

    var getPaymentStatusResult: PaymentStatus = PaymentStatus(card = true, inApp = true, paypal = false)

    var createOmnichannelPaymentTokenCallCount: Int = 0
    var createPaymentTokenCallCount: Int = 0
    var getPaymentTokenStatusCallCount: Int = 0
    var getAvailablePaymentMethodsCallCount: Int = 0
    var getPaymentStatusCallCount: Int = 0

    override suspend fun createOmnichannelPaymentToken(
        sessionUserId: SessionUserId?,
        packageName: String,
        productId: String,
        orderId: String
    ): PaymentTokenResult.CreatePaymentTokenResult {
        createOmnichannelPaymentTokenCallCount++
        return createOmnichannelPaymentTokenResult
    }

    @Deprecated("Use createOmnichannelPaymentToken instead")
    override suspend fun createPaymentToken(
        sessionUserId: SessionUserId?,
        paymentType: PaymentType
    ): PaymentTokenResult.CreatePaymentTokenResult {
        createPaymentTokenCallCount++
        return createPaymentTokenResult
    }

    override suspend fun getPaymentTokenStatus(
        sessionUserId: SessionUserId?,
        paymentToken: ProtonPaymentToken
    ): PaymentTokenResult.PaymentTokenStatusResult {
        getPaymentTokenStatusCallCount++
        return getPaymentTokenStatusResult
    }

    override suspend fun getAvailablePaymentMethods(sessionUserId: SessionUserId): List<PaymentMethod> {
        getAvailablePaymentMethodsCallCount++
        return getAvailablePaymentMethodsResult
    }

    override suspend fun getPaymentStatus(sessionUserId: SessionUserId?, appStore: AppStore): PaymentStatus {
        getPaymentStatusCallCount++
        return getPaymentStatusResult
    }
}
