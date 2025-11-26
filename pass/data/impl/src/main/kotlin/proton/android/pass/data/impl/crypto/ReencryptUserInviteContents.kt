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
import proton.android.pass.data.impl.responses.PendingUserInviteResponse
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

interface ReencryptUserInviteContents {
    suspend operator fun invoke(userId: UserId, invite: PendingUserInviteResponse): ReencryptedInviteContent
}

class ReencryptUserInviteContentsImpl @Inject constructor(
    private val acceptUserInvite: AcceptUserInvite,
    private val userRepository: UserRepository,
    private val userAddressRepository: UserAddressRepository,
    private val getAllKeysByAddress: GetAllKeysByAddress,
    private val inviteContentReencrypter: InviteContentReencrypter
) : ReencryptUserInviteContents {

    override suspend fun invoke(userId: UserId, invite: PendingUserInviteResponse): ReencryptedInviteContent {
        val user = userRepository.getUser(userId)

        val addressId = AddressId(invite.invitedAddressId)
        val address = fetchWithForceRefresh(
            tag = TAG,
            initial = { userAddressRepository.getAddress(userId, addressId) },
            refresh = { userAddressRepository.getAddress(userId, addressId, true) }
        ) ?: error("Could not get invited address")

        val addressPrivateKeys = address.keys.map { it.privateKey }

        val inviteKeys = invite.keys.map { it.toEncryptedInviteKey() }
        val inviterAddressKeys = getAllKeysByAddress(invite.inviterEmail)
            .getOrElse {
                PassLogger.w(TAG, "Could not get inviter address keys")
                PassLogger.w(TAG, it)
                throw it
            }
            .map { it.publicKey }

        val openKeys = acceptUserInvite(
            invitedUser = user,
            invitedUserAddressKeys = addressPrivateKeys,
            inviterAddressKeys = inviterAddressKeys,
            keys = inviteKeys
        )
        val encryptedOpenKey = openKeys.keys.firstOrNull()
            ?: error("No open key found")

        return inviteContentReencrypter.reencrypt(
            localEncryptedKey = encryptedOpenKey.localEncryptedKey,
            encodedContent = invite.vaultData?.content
        )
    }

    companion object {
        private const val TAG = "ReencryptInviteContentsImpl"
    }
}
