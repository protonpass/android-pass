/*
 * Copyright (c) 2025 Proton AG
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
import me.proton.core.user.domain.repository.UserRepository
import proton.android.pass.crypto.api.usecases.invites.AcceptGroupInvite
import proton.android.pass.crypto.api.usecases.invites.OpenOrganizationKey
import proton.android.pass.data.api.repositories.GroupRepository
import proton.android.pass.data.api.usecases.GetAllKeysByAddress
import proton.android.pass.data.impl.local.GroupInviteAndKeysEntity
import proton.android.pass.data.impl.requests.invites.InviteKeyRotation
import proton.android.pass.domain.GroupId
import proton.android.pass.domain.repositories.OrganizationKeyRepository
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

interface EncryptGroupInviteKeys {
    suspend operator fun invoke(userId: UserId, invite: GroupInviteAndKeysEntity): List<InviteKeyRotation>
}

class EncryptGroupInviteKeysImpl @Inject constructor(
    private val acceptGroupInvite: AcceptGroupInvite,
    private val groupRepository: GroupRepository,
    private val organizationKeyRepository: OrganizationKeyRepository,
    private val openOrganizationKey: OpenOrganizationKey,
    private val getAllKeysByAddress: GetAllKeysByAddress,
    private val userRepository: UserRepository
) : EncryptGroupInviteKeys {

    override suspend fun invoke(userId: UserId, invite: GroupInviteAndKeysEntity): List<InviteKeyRotation> {
        val (groupInvite, groupKeys) = invite

        val user = userRepository.getUser(userId)

        val group = fetchWithForceRefresh(
            tag = TAG,
            initial = { groupRepository.retrieveGroup(userId, GroupId(groupInvite.invitedGroupId)) },
            refresh = {
                groupRepository.retrieveGroup(userId, GroupId(groupInvite.invitedGroupId), true)
            }
        ) ?: error("Group not found")

        val groupPrivateKeys = group.address?.keys ?: error("Group doesn't have private keys")

        val organizationKey = fetchWithForceRefresh(
            tag = TAG,
            initial = { organizationKeyRepository.getOrganizationKey(userId) },
            refresh = { organizationKeyRepository.getOrganizationKey(userId, true) }
        ) ?: error("Organization key not found")

        val (organizationPrivateKey, _) = openOrganizationKey(user, organizationKey)
            .getOrElse {
                PassLogger.w(TAG, "Failed to open organization key")
                PassLogger.w(TAG, it)
                throw it
            }

        val inviterAddressKeys = getAllKeysByAddress(groupInvite.inviterEmail)
            .getOrElse {
                PassLogger.w(TAG, "Could not get inviter address keys")
                PassLogger.w(TAG, it)
                throw it
            }
            .map { it.publicKey }

        val encryptedKeys = acceptGroupInvite(
            groupPrivateKeys = groupPrivateKeys,
            organizationPrivateKey = organizationPrivateKey,
            inviterAddressKeys = inviterAddressKeys,
            keys = groupKeys.map { it.toEncryptedInviteKey() }
        )

        return encryptedKeys.map { it.toInviteKeyRotation() }
    }

    companion object {
        private const val TAG = "EncryptGroupInviteKeys"
    }
}
