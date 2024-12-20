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

package proton.android.pass.features.sharing.manage.item.ui

import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.shares.ShareMember
import proton.android.pass.domain.shares.SharePendingInvite

internal sealed interface ManageItemUiEvent {

    data object OnBackClick : ManageItemUiEvent

    data class OnInviteToItemClick(
        internal val shareId: ShareId,
        internal val itemId: ItemId
    ) : ManageItemUiEvent

    @JvmInline
    value class OnInviteToVaultClick(internal val shareId: ShareId) : ManageItemUiEvent

    data class OnPendingInviteOptionsClick(
        internal val shareId: ShareId,
        internal val pendingInvite: SharePendingInvite
    ) : ManageItemUiEvent

    data class OnMemberOptionsClick(
        internal val shareId: ShareId,
        internal val member: ShareMember
    ) : ManageItemUiEvent

}
