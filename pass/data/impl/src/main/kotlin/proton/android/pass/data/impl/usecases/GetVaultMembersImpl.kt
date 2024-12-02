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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.data.api.usecases.GetVaultByShareId
import proton.android.pass.data.api.usecases.GetVaultMembers
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.VaultMember
import proton.android.pass.data.impl.api.PasswordManagerApi
import proton.android.pass.data.impl.responses.ShareMemberResponse
import proton.android.pass.data.impl.responses.ShareNewUserPendingInvite
import proton.android.pass.data.impl.responses.SharePendingInvite
import proton.android.pass.log.api.PassLogger
import proton.android.pass.domain.InviteId
import proton.android.pass.domain.NewUserInviteId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.SharePermissionFlag
import proton.android.pass.domain.ShareRole
import proton.android.pass.domain.hasFlag
import proton.android.pass.domain.toPermissions
import javax.inject.Inject

class GetVaultMembersImpl @Inject constructor(
    private val apiProvider: ApiProvider,
    private val getVaultByShareId: GetVaultByShareId,
    private val observeCurrentUser: ObserveCurrentUser
) : GetVaultMembers {

    override fun invoke(shareId: ShareId): Flow<List<VaultMember>> = observeCurrentUser()
        .flatMapLatest { user ->
            val userId = user.userId
            val vault = getVaultByShareId(userId = userId, shareId = shareId).firstOrNull()
                ?: return@flatMapLatest flowOf(emptyList())

            val vaultPermissions = vault.role.toPermissions()
            if (vaultPermissions.hasFlag(SharePermissionFlag.Admin)) {
                combine(
                    oneShot { fetchShareMembers(shareId, userId, user.email) },
                    oneShot { fetchPendingInvites(shareId, userId) }
                ) { members, invites -> members + invites }
            } else {
                oneShot { fetchShareMembers(shareId, userId, user.email) }
            }
        }

    private suspend fun fetchShareMembers(
        shareId: ShareId,
        userId: UserId,
        userEmail: String?
    ): List<VaultMember.Member> {
        val members = apiProvider.get<PasswordManagerApi>(userId)
            .invoke { getShareMembers(shareId.id) }
            .valueOrThrow
        return members.members.map { it.toDomain(userEmail) }
    }

    private suspend fun fetchPendingInvites(shareId: ShareId, userId: UserId): List<VaultMember> {
        val invites = apiProvider.get<PasswordManagerApi>(userId)
            .invoke { getPendingInvitesForShare(shareId.id) }
            .valueOrThrow
        val inviteList = invites.invites.map { it.toDomain() }
        val newUserInviteList = invites.newUserInvites.map { it.toDomain() }
        return inviteList + newUserInviteList
    }

    private fun ShareMemberResponse.toDomain(currentUserEmail: String?) = VaultMember.Member(
        shareId = ShareId(shareId),
        email = userEmail,
        username = userName,
        role = shareRoleId?.let { ShareRole.fromValue(it) },
        isCurrentUser = userEmail == currentUserEmail,
        isOwner = owner == true
    )

    private fun ShareNewUserPendingInvite.toDomain() = VaultMember.NewUserInvitePending(
        email = invitedEmail,
        newUserInviteId = NewUserInviteId(newUserInviteId),
        role = shareRoleId.let { ShareRole.fromValue(it) },
        signature = signature,
        inviteState = when (state) {
            INVITE_STATE_PENDING_ACCOUNT_CREATION -> {
                VaultMember.NewUserInvitePending.InviteState.PendingAccountCreation
            }
            INVITE_STATE_PENDING_ACCEPTANCE -> {
                VaultMember.NewUserInvitePending.InviteState.PendingAcceptance
            }
            else -> {
                PassLogger.w(TAG, "Unknown NewUserInvite state: $state")
                VaultMember.NewUserInvitePending.InviteState.PendingAccountCreation
            }
        }
    )

    private fun SharePendingInvite.toDomain() = VaultMember.InvitePending(
        email = invitedEmail,
        inviteId = InviteId(inviteId)
    )

    companion object {
        private const val TAG = "GetVaultMembersImpl"
        private const val INVITE_STATE_PENDING_ACCOUNT_CREATION = 1
        private const val INVITE_STATE_PENDING_ACCEPTANCE = 2
    }
}
