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
import me.proton.core.payment.domain.entity.Purchase
import me.proton.core.payment.domain.repository.PurchaseRepository
import javax.inject.Inject

@Suppress("NotImplementedDeclaration")
class FakePurchaseRepository @Inject constructor() : PurchaseRepository {
    override fun observePurchase(planName: String): Flow<Purchase?> {
        TODO("Not yet implemented")
    }

    override fun observePurchases(): Flow<List<Purchase>> {
        TODO("Not yet implemented")
    }

    override suspend fun getPurchase(planName: String): Purchase? {
        TODO("Not yet implemented")
    }

    override suspend fun getPurchases(): List<Purchase> {
        TODO("Not yet implemented")
    }

    override suspend fun upsertPurchase(purchase: Purchase) {
        TODO("Not yet implemented")
    }

    override suspend fun deletePurchase(planName: String) {
        TODO("Not yet implemented")
    }

    override fun onPurchaseStateChanged(initialState: Boolean): Flow<Purchase> {
        TODO("Not yet implemented")
    }
}
