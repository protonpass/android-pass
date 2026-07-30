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

package proton.android.pass.payments.api

import kotlinx.coroutines.flow.Flow

interface Payments {
    val supportsStorePayments: Boolean

    suspend fun getProducts(): Result<List<PaymentProduct>>

    fun observeSessionState(): Flow<PaymentSessionState>

    suspend fun purchase(productId: String, offerToken: String): Result<Unit>
}

data class PaymentProduct(
    val id: String,
    val offers: List<PaymentOffer>
)

data class PaymentOffer(
    val token: String,
    val isDiscounted: Boolean,
    val pricingPhases: List<PaymentPricingPhase>
)

data class PaymentPricingPhase(
    val amount: Long,
    val currency: String,
    val formattedAmount: String,
    val period: String
)

sealed interface PaymentSessionState {
    data object Idle : PaymentSessionState
    data object Processing : PaymentSessionState
    data class Reconciled(val purchase: ReconciledPayment) : PaymentSessionState
    data class Failure(val isStoreError: Boolean) : PaymentSessionState
}

data class ReconciledPayment(
    val planId: String,
    val cycle: Int?,
    val orderId: String
)
