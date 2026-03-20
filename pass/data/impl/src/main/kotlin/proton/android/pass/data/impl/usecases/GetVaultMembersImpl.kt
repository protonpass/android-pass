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

package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.getOrNull
import proton.android.pass.log.api.PassLogger
import proton.android.pass.data.api.repositories.ShareMembersRepository
import proton.android.pass.data.api.usecases.GetVaultMembers
import proton.android.pass.data.api.usecases.GroupMembers
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.ObserveGroupMembersByGroup
import proton.android.pass.data.api.usecases.VaultMember
import proton.android.pass.data.api.usecases.shares.ObserveSharePendingInvites
import proton.android.pass.domain.NewUserInviteId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.shares.ShareMember
import proton.android.pass.domain.shares.SharePendingInvite
import javax.inject.Inject

class GetVaultMembersImpl @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUser,
    private val observeSharePendingInvites: ObserveSharePendingInvites,
    private val observeGroupMembersByGroup: ObserveGroupMembersByGroup,
    private val shareMembersRepository: ShareMembersRepository
) : GetVaultMembers {

    override fun invoke(shareId: ShareId): Flow<List<VaultMember>> = combine(
        observeCurrentUser(),
        observeSharePendingInvites(shareId),
        observeGroupMembersByGroup().asLoadingResult()
    ) { user, pendingInvites, groupMembersResult ->
        if (groupMembersResult is LoadingResult.Error) {
            PassLogger.w(TAG, "Failed to load group members, group info will be missing from vault member list")
            PassLogger.w(TAG, groupMembersResult.exception)
        }
        val groupByEmail = groupMembersResult.getOrNull().orEmpty().toGroupByEmail()
        val shareMembers = shareMembersRepository.getShareMembers(
            userId = user.userId,
            shareId = shareId,
            userEmail = user.email
        )
        buildVaultMembers(pendingInvites, shareMembers, groupByEmail)
    }

    companion object {
        private const val TAG = "GetVaultMembersImpl"
    }
}

private fun List<GroupMembers>.toGroupByEmail(): Map<String, GroupMembers> = mapNotNull { groupMembers ->
    groupMembers.group.groupEmail?.let { email -> email to groupMembers }
}.toMap()

private fun buildVaultMembers(
    pendingInvites: List<SharePendingInvite>,
    shareMembers: List<ShareMember>,
    groupByEmail: Map<String, GroupMembers>
): List<VaultMember> = buildList {
    pendingInvites
        .map(SharePendingInvite::toVaultMember)
        .also(::addAll)

    shareMembers
        .map { it.toVaultMember(groupByEmail) }
        .also(::addAll)
}

private fun ShareMember.toVaultMember(groupByEmail: Map<String, GroupMembers>): VaultMember {
    val groupInfo = if (isGroup) groupByEmail[email] else null
    return VaultMember.Member(
        shareId = shareId,
        email = email,
        groupId = groupInfo?.group?.id,
        username = groupInfo?.group?.name ?: username,
        role = role,
        isCurrentUser = isCurrentUser,
        isOwner = isOwner,
        isGroup = isGroup,
        memberCount = groupInfo?.members?.size ?: 0
    )
}

private fun SharePendingInvite.toVaultMember(): VaultMember = when (this) {
    is SharePendingInvite.ExistingUser -> VaultMember.InvitePending(
        email = email,
        inviteId = inviteId
    )

    is SharePendingInvite.NewUser -> VaultMember.NewUserInvitePending(
        email = email,
        newUserInviteId = NewUserInviteId(inviteId.value),
        role = role,
        signature = signature,
        inviteState = when (inviteState) {
            SharePendingInvite.NewUser.InviteState.PendingAccountCreation -> {
                VaultMember.NewUserInvitePending.InviteState.PendingAccountCreation
            }

            SharePendingInvite.NewUser.InviteState.PendingAcceptance -> {
                VaultMember.NewUserInvitePending.InviteState.PendingAcceptance
            }
        }
    )
}
