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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapNotNull
import me.proton.core.payment.domain.entity.Purchase
import me.proton.core.payment.domain.repository.PurchaseRepository
import javax.inject.Inject

class FakePurchaseRepository @Inject constructor() : PurchaseRepository {

    private val purchases = MutableStateFlow<List<Purchase>>(emptyList())
    private val purchaseStateChanged = MutableSharedFlow<Purchase>(extraBufferCapacity = 64)

    override fun observePurchase(planName: String): Flow<Purchase?> =
        purchases.mapNotNull { list -> list.firstOrNull { it.planName == planName } }

    override fun observePurchases(): Flow<List<Purchase>> = purchases

    override suspend fun getPurchase(planName: String): Purchase? =
        purchases.value.firstOrNull { it.planName == planName }

    override suspend fun getPurchases(): List<Purchase> = purchases.value

    override suspend fun upsertPurchase(purchase: Purchase) {
        purchases.value = purchases.value
            .filterNot { it.planName == purchase.planName } + purchase
        purchaseStateChanged.emit(purchase)
    }

    override suspend fun deletePurchase(planName: String) {
        purchases.value = purchases.value.filterNot { it.planName == planName }
    }

    override fun onPurchaseStateChanged(initialState: Boolean): Flow<Purchase> = purchaseStateChanged
}
