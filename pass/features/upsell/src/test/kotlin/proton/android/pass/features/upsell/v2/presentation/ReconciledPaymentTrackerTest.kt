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

package proton.android.pass.features.upsell.v2.presentation

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import proton.android.pass.commonui.fakes.FakeSavedStateHandleProvider
import proton.android.pass.payments.api.ReconciledPayment

class ReconciledPaymentTrackerTest {

    @Test
    fun `does not consume the same purchase twice after recreation`() {
        val savedStateHandleProvider = FakeSavedStateHandleProvider()
        val purchase = ReconciledPayment(planId = "pass_plus", cycle = 12, orderId = "order-id")

        assertThat(ReconciledPaymentTracker(savedStateHandleProvider).consume(purchase)).isEqualTo(true)
        assertThat(ReconciledPaymentTracker(savedStateHandleProvider).consume(purchase)).isEqualTo(false)
    }
}
