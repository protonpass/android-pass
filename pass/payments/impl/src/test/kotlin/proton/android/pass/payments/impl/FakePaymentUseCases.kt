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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import me.proton.android.payment.product.model.Product
import me.proton.android.payment.product.usecase.GetProducts
import me.proton.android.payment.purchase.model.PendingPurchase
import me.proton.android.payment.purchase.model.SessionState
import me.proton.android.payment.purchase.usecase.ObserveSessionState
import me.proton.android.payment.purchase.usecase.PurchaseProduct

internal class FakeGetProducts : GetProducts {
    override suspend fun invoke(): Result<List<Product>> = Result.success(emptyList())
}

internal class FakeObserveSessionState(
    var states: Flow<SessionState> = flowOf(SessionState.Idle)
) : ObserveSessionState {
    override fun invoke(): Flow<SessionState> = states
}

internal class FakePurchaseProduct : PurchaseProduct {
    override suspend fun invoke(purchase: PendingPurchase): Result<SessionState.Purchasing.Terminal> =
        Result.success(SessionState.Purchasing.Terminal.ReadyToReconcile)
}
