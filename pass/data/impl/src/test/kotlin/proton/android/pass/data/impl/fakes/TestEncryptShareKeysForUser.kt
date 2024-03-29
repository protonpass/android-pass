/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.data.impl.fakes

import me.proton.core.user.domain.entity.UserAddress
import proton.android.pass.crypto.api.usecases.EncryptedInviteShareKeyList
import proton.android.pass.data.impl.crypto.EncryptShareKeysForUser
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.key.ShareKey

class TestEncryptShareKeysForUser : EncryptShareKeysForUser {

    private var result: Result<EncryptedInviteShareKeyList> = Result.success(EncryptedInviteShareKeyList(emptyList()))

    private val memory = mutableListOf<Payload>()

    fun getMemory(): List<Payload> = memory

    fun setResult(result: Result<EncryptedInviteShareKeyList>) {
        this.result = result
    }

    override suspend fun invoke(
        userAddress: UserAddress,
        shareId: ShareId,
        targetEmail: String,
        shareKeys: List<ShareKey>
    ): Result<EncryptedInviteShareKeyList> {
        memory.add(Payload(userAddress, shareId, targetEmail, shareKeys))
        return result
    }

    override suspend fun invoke(
        userAddress: UserAddress,
        shareId: ShareId,
        targetEmail: String
    ) = invoke(userAddress, shareId, targetEmail, emptyList())

    data class Payload(
        val userAddress: UserAddress,
        val shareId: ShareId,
        val targetEmail: String,
        val shareKeys: List<ShareKey>
    )
}
