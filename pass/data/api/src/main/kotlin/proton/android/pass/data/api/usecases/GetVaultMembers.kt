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

package proton.android.pass.data.api.usecases

import kotlinx.coroutines.flow.Flow
import proton.android.pass.domain.InviteId
import proton.android.pass.domain.NewUserInviteId
import proton.android.pass.domain.GroupId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole

sealed class VaultMember(open val email: String) {
    data class Member(
        override val email: String,
        val shareId: ShareId,
        val groupId: GroupId? = null,
        val username: String,
        val isGroup: Boolean,
        val memberCount: Int,
        val role: ShareRole?,
        val isCurrentUser: Boolean,
        val isCurrentUserMember: Boolean,
        val isOwner: Boolean
    ) : VaultMember(email)

    data class InvitePending(
        override val email: String,
        val inviteId: InviteId,
        val displayName: String = email,
        val groupId: GroupId? = null,
        val memberCount: Int = 0
    ) : VaultMember(email)

    data class NewUserInvitePending(
        override val email: String,
        val newUserInviteId: NewUserInviteId,
        val role: ShareRole,
        val signature: String,
        val inviteState: InviteState,
        val displayName: String = email,
        val groupId: GroupId? = null,
        val memberCount: Int = 0
    ) : VaultMember(email) {

        enum class InviteState {
            PendingAccountCreation,
            PendingAcceptance
        }
    }
}

interface GetVaultMembers {
    operator fun invoke(shareId: ShareId): Flow<List<VaultMember>>
}
