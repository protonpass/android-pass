/*
 * Copyright (c) 2023-2026 Proton AG
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
import proton.android.pass.data.impl.responses.invites.GroupInviteApiModel
import proton.android.pass.domain.GroupId
import javax.inject.Inject

interface ReencryptGroupInviteContents {
    suspend operator fun invoke(userId: UserId, invite: GroupInviteApiModel): ReencryptedInviteContent
}

class ReencryptGroupInviteContentsImpl @Inject constructor(
    private val acceptGroupInvite: AcceptGroupInvite,
    private val inviteContentReencrypter: InviteContentReencrypter,
    private val resolveGroupInviteCryptoContext: ResolveGroupInviteCryptoContext
) : ReencryptGroupInviteContents {

    override suspend fun invoke(userId: UserId, invite: GroupInviteApiModel): ReencryptedInviteContent {
        val cryptoContext = resolveGroupInviteCryptoContext(
            userId = userId,
            groupId = GroupId(invite.invitedGroupId),
            inviterEmail = invite.inviterEmail,
            isGroupOwner = invite.isGroupOwner
        )

        return try {
            val inviteKeys = invite.keys.map { it.toEncryptedInviteKey() }
            val openKeys = acceptGroupInvite(
                user = cryptoContext.user,
                groupPrivateKeys = cryptoContext.groupPrivateKeys,
                unlockedOrganizationKey = cryptoContext.unlockedOrganizationKey,
                inviterAddressKeys = cryptoContext.inviterPublicKeys,
                keys = inviteKeys,
                isGroupOwner = cryptoContext.isGroupOwner
            )
            val reencryptedKey = openKeys.firstOrNull() ?: error("No open key found")

            inviteContentReencrypter.reencrypt(
                localEncryptedKey = reencryptedKey.localEncryptedKey,
                encodedContent = invite.vaultData?.content
            )
        } finally {
            cryptoContext.unlockedOrganizationKey?.close()
        }
    }

}
