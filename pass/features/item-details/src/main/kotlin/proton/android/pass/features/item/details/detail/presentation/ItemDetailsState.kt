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

package proton.android.pass.features.item.details.detail.presentation

import androidx.compose.runtime.Stable
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsActionForbiddenReason
import proton.android.pass.commonuimodels.api.items.ItemDetailState
import proton.android.pass.data.api.usecases.ItemActions
import proton.android.pass.data.api.usecases.capabilities.CanShareVaultStatus
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.Share
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole

@Stable
internal sealed interface ItemDetailsState {

    val event: ItemDetailsEvent

    data object Error : ItemDetailsState {

        override val event: ItemDetailsEvent = ItemDetailsEvent.Idle

    }

    data object Loading : ItemDetailsState {

        override val event: ItemDetailsEvent = ItemDetailsEvent.Idle

    }

    data class Success(
        internal val shareId: ShareId,
        internal val itemId: ItemId,
        internal val itemDetailState: ItemDetailState,
        override val event: ItemDetailsEvent,
        private val itemActions: ItemActions,
        private val itemFeatures: ItemFeatures,
        private val share: Share
    ) : ItemDetailsState {

        internal val canViewItemHistory: Boolean = itemFeatures.isHistoryEnabled
            .and(share.shareRole !is ShareRole.Read)

        internal val isFileAttachmentsEnabled: Boolean = itemFeatures.isFileAttachmentsEnabled

        internal val isItemSharingEnabled: Boolean = itemFeatures.isItemSharingEnabled

        internal val shareSharedCount: Int = share.memberCount.plus(itemDetailState.itemShareCount)

        internal val isEditEnabled: Boolean =
            itemActions.canEdit is ItemActions.CanEditActionState.Enabled

        internal val cannotEditReason: ItemDetailsActionForbiddenReason? =
            when (val canEdit = itemActions.canEdit) {
                ItemActions.CanEditActionState.Enabled -> null
                is ItemActions.CanEditActionState.Disabled -> when (canEdit.reason) {
                    ItemActions.CanEditActionState.CanEditDisabledReason.Downgraded -> {
                        ItemDetailsActionForbiddenReason.EditItemUpgradeRequired
                    }

                    ItemActions.CanEditActionState.CanEditDisabledReason.ItemInTrash -> {
                        ItemDetailsActionForbiddenReason.EditItemTrashed
                    }

                    ItemActions.CanEditActionState.CanEditDisabledReason.NotEnoughPermission -> {
                        ItemDetailsActionForbiddenReason.EditItemPermissionRequired
                    }
                }
            }

        internal val isShareEnabled: Boolean = itemActions.canShare.value()

        internal val cannotShareReason: ItemDetailsActionForbiddenReason? =
            when (val canShare = itemActions.canShare) {
                is CanShareVaultStatus.CanShare -> null
                is CanShareVaultStatus.CannotShare -> when (canShare.reason) {
                    CanShareVaultStatus.CannotShareReason.ItemInTrash -> {
                        ItemDetailsActionForbiddenReason.ShareItemTrashed
                    }

                    CanShareVaultStatus.CannotShareReason.NotEnoughInvites -> {
                        ItemDetailsActionForbiddenReason.ShareItemLimitReached
                    }

                    CanShareVaultStatus.CannotShareReason.NotEnoughPermissions -> {
                        ItemDetailsActionForbiddenReason.ShareItemPermissionRequired
                    }

                    CanShareVaultStatus.CannotShareReason.Unknown -> null
                }
            }

    }

}
