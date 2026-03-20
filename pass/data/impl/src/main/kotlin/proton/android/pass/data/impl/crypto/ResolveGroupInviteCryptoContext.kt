/*
 * Copyright (c) 2025-2026 Proton AG
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

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.pgp.UnlockedKey
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.key.PrivateAddressKey
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.repository.UserRepository
import proton.android.pass.crypto.api.usecases.invites.OpenOrganizationKey
import proton.android.pass.data.api.repositories.GroupRepository
import proton.android.pass.data.api.usecases.GetAllKeysByAddress
import proton.android.pass.domain.GroupId
import proton.android.pass.domain.repositories.OrganizationKeyRepository
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

data class GroupInviteCryptoContext(
    val user: User,
    val groupPrivateKeys: List<PrivateAddressKey>,
    val unlockedOrganizationKey: UnlockedKey?,
    val inviterPublicKeys: List<PublicKey>,
    val groupPublicKeys: List<PublicKey>,
    val isGroupOwner: Boolean
)

interface ResolveGroupInviteCryptoContext {
    suspend operator fun invoke(
        userId: UserId,
        groupId: GroupId,
        inviterEmail: String,
        isGroupOwner: Boolean
    ): GroupInviteCryptoContext
}

class ResolveGroupInviteCryptoContextImpl @Inject constructor(
    private val cryptoContext: CryptoContext,
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository,
    private val organizationKeyRepository: OrganizationKeyRepository,
    private val openOrganizationKey: OpenOrganizationKey,
    private val getAllKeysByAddress: GetAllKeysByAddress
) : ResolveGroupInviteCryptoContext {

    override suspend fun invoke(
        userId: UserId,
        groupId: GroupId,
        inviterEmail: String,
        isGroupOwner: Boolean
    ): GroupInviteCryptoContext {
        val user = userRepository.getUser(userId)
        val unlockedOrganizationKey: UnlockedKey? = if (!isGroupOwner) {
            val orgPrivateKey = fetchOrganizationPrivateKey(userId, user)
            cryptoContext.pgpCrypto.unlock(
                privateKey = orgPrivateKey.key,
                passphrase = ByteArray(0)
            )
        } else null
        val group = fetchWithForceRefresh(
            tag = TAG,
            initial = { groupRepository.retrieveGroup(userId, groupId) },
            refresh = { groupRepository.retrieveGroup(userId, groupId, true) }
        ) ?: error("Group not found")

        val groupPrivateKeys = group.address?.keys ?: error("Group doesn't have private keys")

        val inviterPublicKeys = getAllKeysByAddress(inviterEmail)
            .getOrElse {
                PassLogger.w(TAG, "Could not get inviter address keys")
                PassLogger.w(TAG, it)
                throw it
            }
            .map { it.publicKey }

        val groupEmail = group.address?.email ?: error("Group doesn't have an address")
        val groupPublicKeys = getAllKeysByAddress(groupEmail)
            .getOrElse {
                PassLogger.w(TAG, "Could not get group address keys")
                PassLogger.w(TAG, it)
                throw it
            }
            .map { it.publicKey }

        return GroupInviteCryptoContext(
            user = user,
            groupPrivateKeys = groupPrivateKeys,
            unlockedOrganizationKey = unlockedOrganizationKey,
            inviterPublicKeys = inviterPublicKeys,
            groupPublicKeys = groupPublicKeys,
            isGroupOwner = isGroupOwner
        )
    }

    private suspend fun fetchOrganizationPrivateKey(userId: UserId, user: User): PrivateKey {
        val organizationKey = fetchWithForceRefresh(
            tag = TAG,
            initial = { organizationKeyRepository.getOrganizationKey(userId) },
            refresh = { organizationKeyRepository.getOrganizationKey(userId, true) }
        )

        if (organizationKey == null) {
            PassLogger.w(TAG, "Organization key not found for user ${userId.id}")
            error("Organization key not found. This user may not have organization access. Please sync and try again.")
        }

        return openOrganizationKey(user, organizationKey)
            .onFailure { error ->
                PassLogger.e(TAG, error, "Failed to unlock organization key for user ${userId.id}")
                throw IllegalStateException(
                    "Cannot unlock organization key. Please verify your master password and try again.",
                    error
                )
            }
            .getOrThrow()
            .first
    }

    companion object {
        private const val TAG = "ResolveGroupInviteCryptoContext"
    }
}
