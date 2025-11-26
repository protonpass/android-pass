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

package proton.android.pass.data.impl.crypto

import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.repository.UserAddressRepository
import me.proton.core.user.domain.repository.UserRepository
import proton.android.pass.crypto.api.usecases.invites.AcceptUserInvite
import proton.android.pass.data.api.usecases.GetAllKeysByAddress
import proton.android.pass.data.impl.local.UserInviteAndKeysEntity
import proton.android.pass.data.impl.requests.invites.InviteKeyRotation
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

interface EncryptUserInviteKeys {
    suspend operator fun invoke(userId: UserId, invite: UserInviteAndKeysEntity): List<InviteKeyRotation>
}

class EncryptUserInviteKeysImpl @Inject constructor(
    private val acceptUserInvite: AcceptUserInvite,
    private val userRepository: UserRepository,
    private val userAddressRepository: UserAddressRepository,
    private val getAllKeysByAddress: GetAllKeysByAddress
) : EncryptUserInviteKeys {
    override suspend fun invoke(userId: UserId, invite: UserInviteAndKeysEntity): List<InviteKeyRotation> {
        val (userInvite, userKeys) = invite
        val user = userRepository.getUser(userId)

        val addressId = AddressId(userInvite.invitedAddressId)
        val address = fetchWithForceRefresh(
            tag = TAG,
            initial = { userAddressRepository.getAddress(userId, addressId) },
            refresh = { userAddressRepository.getAddress(userId, addressId, refresh = true) }
        ) ?: error("Could not get invited address")

        val inviterAddressKeys = getAllKeysByAddress(userInvite.inviterEmail)
            .getOrElse {
                PassLogger.w(TAG, "Could not get inviter address keys")
                PassLogger.w(TAG, it)
                throw it
            }
            .map { it.publicKey }

        val encryptedKeys = acceptUserInvite(
            invitedUser = user,
            invitedUserAddressKeys = address.keys.map { it.privateKey },
            inviterAddressKeys = inviterAddressKeys,
            keys = userKeys.map { it.toEncryptedInviteKey() }
        )

        return encryptedKeys.keys.map { it.toInviteKeyRotation() }
    }

    companion object {
        private const val TAG = "EncryptUserInviteKeysImpl"
    }
}
