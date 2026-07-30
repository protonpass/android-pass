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

package proton.android.pass.payment

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Test
import proton.android.pass.payments.api.PaymentSessionState
import proton.android.pass.payments.api.ReconciledPayment
import proton.android.pass.payments.fakes.FakePayments

class PaymentReconciliationHandlerTest {

    @Test
    fun `refreshes primary user access when a payment is reconciled`() = runTest {
        val payments = FakePayments()
        val userId = UserId("user-id")
        val refreshedUserIds = mutableListOf<UserId>()

        PaymentReconciliationHandler(
            scope = backgroundScope,
            payments = payments,
            getPrimaryUserId = { userId },
            refreshUserAccess = { refreshedUserIds += it }
        ).start()

        payments.sessionState.value = PaymentSessionState.Reconciled(
            ReconciledPayment("plan-id", 12, "order-id")
        )
        runCurrent()

        assertThat(refreshedUserIds).containsExactly(userId)
    }

    @Test
    fun `does not refresh user access for a non-reconciled payment state`() = runTest {
        val payments = FakePayments()
        val refreshedUserIds = mutableListOf<UserId>()

        PaymentReconciliationHandler(
            scope = backgroundScope,
            payments = payments,
            getPrimaryUserId = { UserId("user-id") },
            refreshUserAccess = { refreshedUserIds += it }
        ).start()

        payments.sessionState.value = PaymentSessionState.Processing
        runCurrent()

        assertThat(refreshedUserIds).isEmpty()
    }

    @Test
    fun `does not refresh user access when no primary user is available`() = runTest {
        val payments = FakePayments()
        val refreshedUserIds = mutableListOf<UserId>()

        PaymentReconciliationHandler(
            scope = backgroundScope,
            payments = payments,
            getPrimaryUserId = { null },
            refreshUserAccess = { refreshedUserIds += it }
        ).start()

        payments.sessionState.value = PaymentSessionState.Reconciled(
            ReconciledPayment("plan-id", 12, "order-id")
        )
        runCurrent()

        assertThat(refreshedUserIds).isEmpty()
    }
}
