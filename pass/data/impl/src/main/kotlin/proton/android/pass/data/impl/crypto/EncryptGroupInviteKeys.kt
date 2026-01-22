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
import proton.android.pass.crypto.api.usecases.invites.AcceptGroupInvite
import proton.android.pass.data.impl.local.GroupInviteAndKeysEntity
import proton.android.pass.data.impl.requests.invites.InviteKeyRotation
import proton.android.pass.domain.GroupId
import javax.inject.Inject

interface EncryptGroupInviteKeys {
    suspend operator fun invoke(userId: UserId, invite: GroupInviteAndKeysEntity): List<InviteKeyRotation>
}

class EncryptGroupInviteKeysImpl @Inject constructor(
    private val acceptGroupInvite: AcceptGroupInvite,
    private val resolveGroupInviteCryptoContext: ResolveGroupInviteCryptoContext
) : EncryptGroupInviteKeys {

    override suspend fun invoke(userId: UserId, invite: GroupInviteAndKeysEntity): List<InviteKeyRotation> {
        val (groupInvite, groupKeys) = invite

        val cryptoContext = resolveGroupInviteCryptoContext(
            userId = userId,
            groupId = GroupId(groupInvite.invitedGroupId),
            inviterEmail = groupInvite.inviterEmail,
            isGroupOwner = groupInvite.isGroupOwner
        )

        val encryptedKeys = acceptGroupInvite(
            groupPrivateKeys = cryptoContext.groupPrivateKeys,
            openerKeys = cryptoContext.openerKeys,
            inviterAddressKeys = cryptoContext.inviterPublicKeys,
            keys = groupKeys.map { it.toEncryptedInviteKey() }
        )

        return encryptedKeys.map { it.toInviteKeyRotation() }
    }
}
