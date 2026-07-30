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

import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.payments.api.ReconciledPayment

internal class ReconciledPaymentTracker(
    private val savedStateHandleProvider: SavedStateHandleProvider
) {

    fun consume(purchase: ReconciledPayment): Boolean {
        val savedStateHandle = savedStateHandleProvider.get()
        if (savedStateHandle.get<String>(LAST_HANDLED_ORDER_ID_KEY) == purchase.orderId) return false

        savedStateHandle[LAST_HANDLED_ORDER_ID_KEY] = purchase.orderId
        return true
    }

    private companion object {
        private const val LAST_HANDLED_ORDER_ID_KEY = "upsell.lastHandledOrderId"
    }
}
