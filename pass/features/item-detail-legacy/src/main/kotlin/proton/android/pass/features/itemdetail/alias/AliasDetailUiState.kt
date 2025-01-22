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

package proton.android.pass.features.itemdetail.alias

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.PersistentList
import proton.android.pass.common.api.Option
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.ItemActions
import proton.android.pass.domain.AliasMailbox
import proton.android.pass.domain.AliasStats
import proton.android.pass.domain.Share
import proton.android.pass.domain.ShareRole
import proton.android.pass.features.itemdetail.common.AliasItemFeatures
import proton.android.pass.features.itemdetail.common.ItemDetailEvent
import proton.android.pass.features.itemdetail.common.ShareClickAction

internal sealed interface AliasDetailUiState {

    @Stable
    data object NotInitialised : AliasDetailUiState

    @Stable
    data object Error : AliasDetailUiState

    @Stable
    data object Pending : AliasDetailUiState

    @Stable
    data class Success(
        val itemUiModel: ItemUiModel,
        val share: Share,
        val mailboxes: PersistentList<AliasMailbox>,
        val isAliasCreatedByUser: Boolean,
        val slNote: String,
        val displayName: String,
        val stats: Option<AliasStats>,
        val contactsCount: Int,
        val isLoadingMap: Map<LoadingStateKey, IsLoadingState>,
        val isLoadingMailboxes: Boolean,
        val isItemSentToTrash: Boolean,
        val isPermanentlyDeleted: Boolean,
        val isRestoredFromTrash: Boolean,
        val canPerformActions: Boolean,
        val shareClickAction: ShareClickAction,
        val itemActions: ItemActions,
        val event: ItemDetailEvent,
        val itemFeatures: AliasItemFeatures,
        val hasMoreThanOneVault: Boolean,
        val attachmentsState: AttachmentsState
    ) : AliasDetailUiState {

        private val isVaultShare: Boolean = share is Share.Vault

        internal val canViewItemHistory: Boolean = itemFeatures.isHistoryEnabled
            .and(share.shareRole !is ShareRole.Read)

        internal val canMigrate: Boolean = isVaultShare && itemActions.canMoveToOtherVault.value()

        internal val canMoveToTrash: Boolean = itemActions.canMoveToTrash

        internal val canLeaveItem: Boolean = !isVaultShare

        internal val requiresBackNavigation: Boolean = isItemSentToTrash ||
            isPermanentlyDeleted ||
            isRestoredFromTrash

        internal val isAnyLoading: Boolean
            get() = isLoadingMap.values.any { it.value() }

        internal val shareSharedCount: Int = share.memberCount.plus(itemUiModel.shareCount)

        internal fun isLoading(key: LoadingStateKey): Boolean = isLoadingMap[key]?.value() ?: false

    }

}
