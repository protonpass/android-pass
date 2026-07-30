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

package proton.android.pass.payments.impl

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import me.proton.android.payment.common.exception.PaymentException
import me.proton.android.payment.purchase.model.ReconciledPurchase
import me.proton.android.payment.purchase.model.SessionState
import org.junit.Test
import proton.android.pass.payments.api.PaymentSessionState
import proton.android.pass.payments.api.ReconciledPayment

class PaymentsImplTest {

    @Test
    fun `maps all payment session states`() = runTest {
        val storeError = PaymentException.StoreError(IllegalStateException())
        val networkError = PaymentException.NetworkError(IllegalStateException())
        val purchase = ReconciledPurchase(
            orderId = "order-id",
            planId = "pass_plus",
            title = "Pass Plus",
            cycle = 12
        )
        val observeSessionState = FakeObserveSessionState(
            flowOf(
                SessionState.Idle,
                SessionState.Purchasing.InFlight.Purchasing,
                SessionState.Reconciling.Terminal.Success(purchase),
                SessionState.Purchasing.Terminal.Failure(storeError),
                SessionState.Purchasing.Terminal.Failure(networkError),
                SessionState.Reconciling.Terminal.Failure(storeError),
                SessionState.Reconciling.Terminal.Failure(networkError)
            )
        )
        val instance = PaymentsImpl(
            getPaymentProducts = FakeGetProducts(),
            purchaseProduct = FakePurchaseProduct(),
            observePaymentSessionState = observeSessionState
        )

        assertThat(instance.observeSessionState().toList()).containsExactly(
            PaymentSessionState.Idle,
            PaymentSessionState.Processing,
            PaymentSessionState.Reconciled(
                ReconciledPayment(planId = "pass_plus", cycle = 12, orderId = "order-id")
            ),
            PaymentSessionState.Failure(isStoreError = true),
            PaymentSessionState.Failure(isStoreError = false),
            PaymentSessionState.Failure(isStoreError = true),
            PaymentSessionState.Failure(isStoreError = false)
        ).inOrder()
    }
}
