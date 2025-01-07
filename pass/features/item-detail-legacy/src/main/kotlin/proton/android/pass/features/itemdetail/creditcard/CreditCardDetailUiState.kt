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

package proton.android.pass.features.itemdetail.creditcard

import androidx.compose.runtime.Stable
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.data.api.usecases.ItemActions
import proton.android.pass.domain.Share
import proton.android.pass.domain.ShareRole
import proton.android.pass.features.itemdetail.common.CreditCardItemFeatures
import proton.android.pass.features.itemdetail.common.ItemDetailEvent
import proton.android.pass.features.itemdetail.common.ShareClickAction

internal sealed interface CreditCardDetailUiState {

    @Stable
    data object NotInitialised : CreditCardDetailUiState

    @Stable
    data object Error : CreditCardDetailUiState

    @Stable
    data object Pending : CreditCardDetailUiState

    @Stable
    data class Success(
        val itemContent: ItemContent,
        val share: Share,
        val isLoading: Boolean,
        val isItemSentToTrash: Boolean,
        val isPermanentlyDeleted: Boolean,
        val isRestoredFromTrash: Boolean,
        val isDowngradedMode: Boolean,
        val canPerformActions: Boolean,
        val shareClickAction: ShareClickAction,
        val itemActions: ItemActions,
        val event: ItemDetailEvent,
        val itemFeatures: CreditCardItemFeatures,
        val attachmentsState: AttachmentsState,
        val hasMoreThanOneVault: Boolean
    ) : CreditCardDetailUiState {

        private val isVaultShare: Boolean = share is Share.Vault

        internal val canViewItemHistory: Boolean = itemFeatures.isHistoryEnabled
            .and(share.shareRole !is ShareRole.Read)

        internal val canMigrate: Boolean = isVaultShare && itemActions.canMoveToOtherVault.value()

        internal val canMoveToTrash: Boolean = isVaultShare && itemActions.canMoveToTrash

        internal val canResetHistory: Boolean = itemActions.canResetHistory

        internal val canLeaveItem: Boolean = !isVaultShare

        internal val shareSharedCount: Int = share.memberCount.plus(itemContent.model.shareCount)

    }

    @Stable
    data class ItemContent(
        val model: ItemUiModel,
        val cardNumber: CardNumberState
    )

}
