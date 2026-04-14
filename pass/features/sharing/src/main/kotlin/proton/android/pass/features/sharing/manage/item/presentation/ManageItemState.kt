/*
 * Copyright (c) 2024-2026 Proton AG
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

package proton.android.pass.features.sharing.manage.item.presentation

import androidx.compose.runtime.Stable
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.GroupMembers
import proton.android.pass.domain.GroupMemberState
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.Share
import proton.android.pass.domain.ShareRole
import proton.android.pass.domain.organizations.OrganizationSharingPolicy
import proton.android.pass.domain.shares.ShareMember
import proton.android.pass.domain.shares.SharePendingInvite

@Stable
internal sealed interface ManageItemState {

    val event: ManageItemEvent

    @Stable
    data object Loading : ManageItemState {

        override val event: ManageItemEvent = ManageItemEvent.Idle

    }

    @Stable
    data class Success(
        override val event: ManageItemEvent = ManageItemEvent.Idle,
        internal val itemId: ItemId,
        internal val share: Share,
        internal val itemPendingInvites: List<SharePendingInvite>,
        internal val vaultPendingInvites: List<SharePendingInvite>,
        internal val itemsCount: Int,
        private val members: List<ShareMember>,
        private val isLoadingState: IsLoadingState,
        private val organizationSharingPolicy: OrganizationSharingPolicy,
        private val groupMembers: List<GroupMembers>,
        internal val isRenameAdminToManagerEnabled: Boolean,
        internal val currentUserEmail: String,
        internal val isGroupMembersLoaded: Boolean
    ) : ManageItemState {

        internal val itemMembers: List<ShareMember> = members.filter { it.isItemMember }

        internal val hasItemMembers: Boolean = itemMembers.isNotEmpty()

        internal val vaultMembers: List<ShareMember> = members.filter { it.isVaultMember }

        internal val hasVaultMembers: Boolean = vaultMembers.isNotEmpty()

        internal val hasItemPendingInvites = itemPendingInvites.isNotEmpty()

        internal val hasVaultPendingInvites = vaultPendingInvites.isNotEmpty()

        internal val isLoading: Boolean = isLoadingState.value()

        internal val canInviteMoreToItem = organizationSharingPolicy.canShareItems

        internal val canInviteMoreToVault = true

        internal val groupsByEmail: Map<String, GroupMembers> = groupMembers
            .mapNotNull { gm -> gm.group.groupEmail?.let { email -> email to gm } }
            .toMap()

        // True when the current user has a direct (non-group) membership at the item or vault
        // level. A direct vault member (visible in vaultMembers with isCurrentUser) has admin
        // rights over all items in the vault independently of any item-level group, so they
        // should be able to manage item-level groups they happen to be in.
        internal val currentUserHasDirectItemMembership: Boolean =
            itemMembers.any { it.isCurrentUser && !it.isGroup } ||
                vaultMembers.any { it.isCurrentUser && !it.isGroup }

        internal val currentUserHasDirectVaultMembership: Boolean =
            vaultMembers.any { it.isCurrentUser && !it.isGroup }

        // Vault admin/owner derived from the current user's own vault member entry, or from a
        // group they belong to that has Admin role on the vault.
        internal val isVaultAdmin: Boolean =
            vaultMembers.firstOrNull { it.isCurrentUser }?.role == ShareRole.Admin ||
                vaultMembers.any { it.isGroup && isCurrentUserMemberOf(it) && it.role == ShareRole.Admin }

        // Returns true when the current user is an active member of the group represented by this
        // ShareMember. Defaults to true while group data is still loading so that 3-dot actions
        // stay hidden until membership can be confirmed.
        internal fun isCurrentUserMemberOf(member: ShareMember): Boolean {
            if (!member.isGroup) return false
            if (!isGroupMembersLoaded) return true
            val groupMemberList = groupsByEmail[member.email]?.members ?: return false
            return groupMemberList.any {
                it.email == currentUserEmail && it.state == GroupMemberState.Active.value
            }
        }

    }

}
