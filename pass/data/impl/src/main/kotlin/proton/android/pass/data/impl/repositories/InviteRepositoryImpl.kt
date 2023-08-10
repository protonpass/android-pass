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

package proton.android.pass.data.impl.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.repositories.InviteRepository
import proton.android.pass.data.impl.crypto.EncryptInviteKeys
import proton.android.pass.data.impl.crypto.ReencryptInviteContents
import proton.android.pass.data.impl.db.entities.InviteEntity
import proton.android.pass.data.impl.db.entities.InviteKeyEntity
import proton.android.pass.data.impl.extensions.toDomain
import proton.android.pass.data.impl.local.InviteAndKeysEntity
import proton.android.pass.data.impl.local.LocalInviteDataSource
import proton.android.pass.data.impl.remote.RemoteInviteDataSource
import proton.android.pass.data.impl.requests.AcceptInviteRequest
import proton.android.pass.log.api.PassLogger
import proton.pass.domain.InviteToken
import proton.pass.domain.PendingInvite
import proton_pass_vault_v1.VaultV1
import javax.inject.Inject

class InviteRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteInviteDataSource,
    private val localDatasource: LocalInviteDataSource,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val reencryptInviteContents: ReencryptInviteContents,
    private val encryptInviteKeys: EncryptInviteKeys
) : InviteRepository {
    override fun observeInvites(userId: UserId): Flow<List<PendingInvite>> = localDatasource
        .observeAllInvites(userId)
        .map { entities ->
            encryptionContextProvider.withEncryptionContext {
                entities.map { it.toDomain(this@withEncryptionContext) }
            }
        }

    override suspend fun refreshInvites(userId: UserId): Boolean {
        val remoteInvites = remoteDataSource.fetchInvites(userId)
        val localInvites = localDatasource.observeAllInvites(userId).firstOrNull() ?: emptyList()

        // Remove deleted invites
        val deletedInvites = localInvites.filter { local ->
            remoteInvites.none { remote -> remote.inviteToken == local.token }
        }
        localDatasource.removeInvites(deletedInvites)

        // Insert new invites
        val newInvites = remoteInvites.filter { remote ->
            localInvites.none { local -> local.token == remote.inviteToken }
        }
        val hasNewInvites = newInvites.isNotEmpty()

        val invitesWithKeys: List<InviteAndKeysEntity> = newInvites.map { invite ->
            val vaultData = invite.vaultData
            val reencryptedInviteContent = reencryptInviteContents(userId, invite)
            val inviteEntity = InviteEntity(
                token = invite.inviteToken,
                userId = userId.id,
                inviterEmail = invite.inviterEmail,
                memberCount = vaultData.memberCount,
                itemCount = vaultData.itemCount,
                reminderCount = invite.remindersSent,
                shareContent = vaultData.content,
                shareContentKeyRotation = vaultData.contentKeyRotation,
                shareContentFormatVersion = vaultData.contentFormatVersion,
                createTime = invite.createTime,
                encryptedContent = reencryptedInviteContent,
            )

            val inviteKeys = invite.keys.map { key ->
                InviteKeyEntity(
                    inviteToken = invite.inviteToken,
                    key = key.key,
                    keyRotation = key.keyRotation,
                    createTime = invite.createTime
                )
            }

            InviteAndKeysEntity(
                inviteEntity = inviteEntity,
                inviteKeys = inviteKeys
            )
        }

        localDatasource.storeInvites(invitesWithKeys)
        return hasNewInvites
    }

    override suspend fun acceptInvite(userId: UserId, inviteToken: InviteToken) {
        val invite = localDatasource.getInviteWithKeys(userId, inviteToken).value()
        if (invite == null) {
            PassLogger.w(TAG, "Could not find the invite: ${inviteToken.value}")
            return
        }

        val keys = encryptInviteKeys(userId, invite)
        val request = AcceptInviteRequest(keys)
        remoteDataSource.acceptInvite(userId, inviteToken, request)
        localDatasource.removeInvite(userId, inviteToken)
    }

    override suspend fun rejectInvite(userId: UserId, inviteToken: InviteToken) {
        remoteDataSource.rejectInvite(userId, inviteToken)
        localDatasource.removeInvite(userId, inviteToken)
    }

    private fun InviteEntity.toDomain(encryptionContext: EncryptionContext): PendingInvite {
        val content = encryptionContext.decrypt(encryptedContent)
        val decoded = VaultV1.Vault.parseFrom(content)
        return PendingInvite(
            inviteToken = InviteToken(token),
            inviterEmail = inviterEmail,
            memberCount = memberCount,
            itemCount = itemCount,
            name = decoded.name,
            icon = decoded.display.icon.toDomain(),
            color = decoded.display.color.toDomain()
        )
    }

    companion object {
        private const val TAG = "InviteRepositoryImpl"
    }
}
