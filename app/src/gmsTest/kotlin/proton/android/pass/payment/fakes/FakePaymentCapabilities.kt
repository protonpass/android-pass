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

package proton.android.pass.payment.fakes

import me.proton.android.payment.billing.model.StoreProduct
import me.proton.android.payment.capability.HttpCapability
import me.proton.android.payment.capability.StoreCapability
import me.proton.android.payment.capability.model.HttpResponse

object FakeHttpCapability : HttpCapability {
    override suspend fun get(endpoint: String): HttpResponse = error("Not used")

    override suspend fun post(endpoint: String, body: ByteArray?): HttpResponse = error("Not used")
}

object FakeStoreCapability : StoreCapability {
    override suspend fun getProducts(ids: List<String>): Result<List<StoreProduct>> = error("Not used")

    override suspend fun purchase(
        productId: String,
        offerToken: String,
        userId: String?
    ): Result<Unit> = error("Not used")

    override suspend fun acknowledge(orderId: String): Result<Unit> = error("Not used")
}
