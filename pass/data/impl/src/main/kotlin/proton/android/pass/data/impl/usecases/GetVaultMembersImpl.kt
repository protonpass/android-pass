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

package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.data.api.usecases.GetVaultById
import proton.android.pass.data.api.usecases.GetVaultMembers
import proton.android.pass.data.api.usecases.VaultMember
import proton.android.pass.data.impl.api.PasswordManagerApi
import proton.android.pass.data.impl.responses.ShareMemberResponse
import proton.android.pass.data.impl.responses.SharePendingInvite
import proton.pass.domain.InviteId
import proton.pass.domain.ShareId
import proton.pass.domain.SharePermissionFlag
import proton.pass.domain.ShareRole
import proton.pass.domain.hasFlag
import proton.pass.domain.toPermissions
import javax.inject.Inject

class GetVaultMembersImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val apiProvider: ApiProvider,
    private val getVaultById: GetVaultById
) : GetVaultMembers {

    override fun invoke(shareId: ShareId): Flow<List<VaultMember>> = oneShot {
        accountManager.getPrimaryUserId().filterNotNull().first()
    }.flatMapLatest { userId ->
        val vault = getVaultById(userId = userId, shareId = shareId).firstOrNull()
            ?: return@flatMapLatest flowOf(emptyList())

        val vaultPermissions = vault.role.toPermissions()
        if (vaultPermissions.hasFlag(SharePermissionFlag.Admin)) {
            combine(
                oneShot { fetchShareMembers(shareId, userId) },
                oneShot { fetchPendingInvites(shareId, userId) }
            ) { members, invites -> members + invites }
        } else {
            oneShot { fetchShareMembers(shareId, userId) }
        }
    }

    private suspend fun fetchShareMembers(
        shareId: ShareId,
        userId: UserId
    ): List<VaultMember.Member> {
        val members = apiProvider.get<PasswordManagerApi>(userId)
            .invoke { getVaultMembers(shareId.id) }
            .valueOrThrow
        return members.members.map { it.toDomain() }
    }

    private suspend fun fetchPendingInvites(
        shareId: ShareId,
        userId: UserId
    ): List<VaultMember.InvitePending> {
        val invites = apiProvider.get<PasswordManagerApi>(userId)
            .invoke { getPendingInvitesForShare(shareId.id) }
            .valueOrThrow
        return invites.invites.map { it.toDomain() }
    }

    private fun ShareMemberResponse.toDomain() = VaultMember.Member(
        shareId = ShareId(shareId),
        email = userEmail,
        username = userName,
        role = shareRoleId?.let { ShareRole.fromValue(it) }
    )

    private fun SharePendingInvite.toDomain() = VaultMember.InvitePending(
        email = invitedEmail,
        inviteId = InviteId(inviteId)
    )
}
