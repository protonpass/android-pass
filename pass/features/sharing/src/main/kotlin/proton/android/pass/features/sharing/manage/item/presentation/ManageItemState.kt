/*
 * Copyright (c) 2024 Proton AG
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
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.Share
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
        internal val pendingInvites: List<SharePendingInvite>,
        internal val itemsCount: Int,
        private val members: List<ShareMember>,
        private val isLoadingState: IsLoadingState
    ) : ManageItemState {

        internal val itemMembers: List<ShareMember> = members.filter { it.isItemMember }

        internal val hasItemMembers: Boolean = itemMembers.isNotEmpty()

        internal val vaultMembers: List<ShareMember> = members.filter { it.isVaultMember }

        internal val hasVaultMembers: Boolean = vaultMembers.isNotEmpty()

        internal val hasPendingInvites = pendingInvites.isNotEmpty()

        internal val isLoading: Boolean = isLoadingState.value()

    }

}
