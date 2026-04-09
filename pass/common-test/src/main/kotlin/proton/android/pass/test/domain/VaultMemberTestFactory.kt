/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.test.domain

import proton.android.pass.data.api.usecases.VaultMember
import proton.android.pass.domain.GroupId
import proton.android.pass.domain.InviteId
import proton.android.pass.domain.NewUserInviteId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole

object VaultMemberTestFactory {

    object Member {

        fun create(
            email: String = "member@test.local",
            shareId: ShareId = ShareId("share-id"),
            groupId: GroupId? = null,
            username: String = "member",
            isGroup: Boolean = false,
            memberCount: Int = 0,
            role: ShareRole = ShareRole.Read,
            isCurrentUser: Boolean = false,
            isCurrentUserMember: Boolean = false,
            isOwner: Boolean = false
        ): VaultMember.Member = VaultMember.Member(
            email = email,
            shareId = shareId,
            groupId = groupId,
            username = username,
            isGroup = isGroup,
            memberCount = memberCount,
            role = role,
            isCurrentUser = isCurrentUser,
            isCurrentUserMember = isCurrentUserMember,
            isOwner = isOwner
        )

    }

    object Group {

        fun create(
            email: String = "group@test.local",
            shareId: ShareId = ShareId("share-id"),
            groupId: GroupId = GroupId("group-id"),
            username: String = "group",
            memberCount: Int = 0,
            role: ShareRole = ShareRole.Admin,
            isCurrentUserMember: Boolean = false
        ): VaultMember.Member = VaultMember.Member(
            email = email,
            shareId = shareId,
            groupId = groupId,
            username = username,
            isGroup = true,
            memberCount = memberCount,
            role = role,
            isCurrentUser = false,
            isCurrentUserMember = isCurrentUserMember,
            isOwner = false
        )

    }

    object InvitePending {

        fun create(
            email: String = "invited@test.local",
            inviteId: InviteId = InviteId("invite-id")
        ): VaultMember.InvitePending = VaultMember.InvitePending(
            email = email,
            inviteId = inviteId
        )

    }

    object NewUserInvitePending {

        fun create(
            email: String = "newuser@test.local",
            newUserInviteId: NewUserInviteId = NewUserInviteId("new-user-invite-id"),
            role: ShareRole = ShareRole.Read,
            signature: String = "",
            inviteState: VaultMember.NewUserInvitePending.InviteState =
                VaultMember.NewUserInvitePending.InviteState.PendingAcceptance
        ): VaultMember.NewUserInvitePending = VaultMember.NewUserInvitePending(
            email = email,
            newUserInviteId = newUserInviteId,
            role = role,
            signature = signature,
            inviteState = inviteState
        )

    }

}
