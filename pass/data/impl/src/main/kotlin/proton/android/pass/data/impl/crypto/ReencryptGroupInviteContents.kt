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
import me.proton.core.user.domain.repository.UserRepository
import proton.android.pass.crypto.api.usecases.invites.AcceptGroupInvite
import proton.android.pass.crypto.api.usecases.invites.OpenOrganizationKey
import proton.android.pass.data.api.repositories.GroupRepository
import proton.android.pass.data.api.usecases.GetAllKeysByAddress
import proton.android.pass.data.impl.responses.invites.GroupInviteApiModel
import proton.android.pass.domain.GroupId
import proton.android.pass.domain.repositories.OrganizationKeyRepository
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

interface ReencryptGroupInviteContents {
    suspend operator fun invoke(userId: UserId, invite: GroupInviteApiModel): ReencryptedInviteContent
}

class ReencryptGroupInviteContentsImpl @Inject constructor(
    private val groupRepository: GroupRepository,
    private val organizationKeyRepository: OrganizationKeyRepository,
    private val openOrganizationKey: OpenOrganizationKey,
    private val acceptGroupInvite: AcceptGroupInvite,
    private val getAllKeysByAddress: GetAllKeysByAddress,
    private val inviteContentReencrypter: InviteContentReencrypter,
    private val userRepository: UserRepository
) : ReencryptGroupInviteContents {

    override suspend fun invoke(userId: UserId, invite: GroupInviteApiModel): ReencryptedInviteContent {
        val user = userRepository.getUser(userId)

        val organizationKey = fetchWithForceRefresh(
            tag = TAG,
            initial = { organizationKeyRepository.getOrganizationKey(userId) },
            refresh = { organizationKeyRepository.getOrganizationKey(userId, forceRefresh = true) }
        ) ?: error("Organization key not found")

        val (privateOrgKey, _) = openOrganizationKey(user, organizationKey)
            .getOrElse { error("Failed to open organization key: ${it.message}") }

        val group = fetchWithForceRefresh(
            tag = TAG,
            initial = { groupRepository.retrieveGroup(userId, GroupId(invite.invitedGroupId)) },
            refresh = { groupRepository.retrieveGroup(userId, GroupId(invite.invitedGroupId), true) }
        ) ?: error("Group not found")
        val groupPrivateKeys = group.address?.keys ?: error("Group doesn't have private keys")

        val inviterAddressKeys = getAllKeysByAddress(invite.inviterEmail)
            .getOrElse {
                PassLogger.w(TAG, "Could not get inviter address keys")
                PassLogger.w(TAG, it)
                throw it
            }
            .map { it.publicKey }
        val inviteKeys = invite.keys.map { it.toEncryptedInviteKey() }
        val openKeys = acceptGroupInvite(
            groupPrivateKeys = groupPrivateKeys,
            organizationPrivateKey = privateOrgKey,
            inviterAddressKeys = inviterAddressKeys,
            keys = inviteKeys
        )

        val reencryptedKey = openKeys.firstOrNull() ?: error("No open key found")

        return inviteContentReencrypter.reencrypt(
            localEncryptedKey = reencryptedKey.localEncryptedKey,
            encodedContent = invite.vaultData?.content
        )
    }

    companion object {
        private const val TAG = "ReencryptGroupInviteContentsImpl"
    }
}
