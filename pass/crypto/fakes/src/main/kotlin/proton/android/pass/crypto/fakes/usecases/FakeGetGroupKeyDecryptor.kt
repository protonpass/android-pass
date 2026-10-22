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

package proton.android.pass.crypto.fakes.usecases

import proton.android.pass.crypto.api.usecases.GetGroupKeyDecryptor
import proton.android.pass.crypto.api.usecases.GroupKeyDecryptor
import proton.android.pass.domain.AddressId
import proton.android.pass.domain.UserId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeGetGroupKeyDecryptor @Inject constructor() : GetGroupKeyDecryptor {

    data class Request(val userId: UserId, val addressId: AddressId, val groupEmail: String)

    private val recordedRequests = mutableListOf<Request>()

    var decryptor: GroupKeyDecryptor = FakeGroupKeyDecryptor()
    var failure: Throwable? = null

    val requests: List<Request> get() = recordedRequests

    override suspend fun invoke(
        userId: UserId,
        addressId: AddressId,
        groupEmail: String
    ): GroupKeyDecryptor {
        recordedRequests.add(Request(userId, addressId, groupEmail))
        failure?.let { throw it }
        return decryptor
    }
}
