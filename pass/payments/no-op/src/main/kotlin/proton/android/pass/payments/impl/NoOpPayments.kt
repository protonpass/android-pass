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

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import proton.android.pass.payments.api.PaymentProduct
import proton.android.pass.payments.api.PaymentSessionState
import proton.android.pass.payments.api.Payments
import javax.inject.Inject

class NoOpPayments @Inject constructor() : Payments {
    override val supportsStorePayments: Boolean = false

    override suspend fun getProducts(): Result<List<PaymentProduct>> = Result.success(emptyList())

    override fun observeSessionState(): Flow<PaymentSessionState> = flowOf(PaymentSessionState.Idle)

    override suspend fun purchase(productId: String, offerToken: String): Result<Unit> =
        Result.failure(UnsupportedOperationException("Store purchases are unavailable for this build"))
}

@Module
@InstallIn(SingletonComponent::class)
abstract class NoOpPaymentsModule {
    @Binds
    abstract fun bindPayments(impl: NoOpPayments): Payments
}
