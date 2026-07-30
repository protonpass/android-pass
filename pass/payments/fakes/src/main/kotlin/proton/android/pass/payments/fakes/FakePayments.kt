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

package proton.android.pass.payments.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import proton.android.pass.payments.api.PaymentProduct
import proton.android.pass.payments.api.PaymentSessionState
import proton.android.pass.payments.api.Payments
import javax.inject.Inject

class FakePayments @Inject constructor() : Payments {
    override var supportsStorePayments: Boolean = true
    var productsResult: Result<List<PaymentProduct>> = Result.success(emptyList())
    var getProductsCallCount: Int = 0
    var purchaseResult: Result<Unit> = Result.success(Unit)
    val purchases = mutableListOf<Purchase>()
    val sessionState = MutableStateFlow<PaymentSessionState>(PaymentSessionState.Idle)

    override suspend fun getProducts(): Result<List<PaymentProduct>> {
        getProductsCallCount++
        return productsResult
    }

    override fun observeSessionState(): Flow<PaymentSessionState> = sessionState

    override suspend fun purchase(productId: String, offerToken: String): Result<Unit> {
        purchases += Purchase(productId = productId, offerToken = offerToken)
        return purchaseResult
    }

    data class Purchase(
        val productId: String,
        val offerToken: String
    )
}
