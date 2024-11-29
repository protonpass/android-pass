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

package proton.android.pass.featureitemdetail.impl.creditcard

import androidx.compose.runtime.Stable
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.data.api.usecases.ItemActions
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.domain.Share
import proton.android.pass.featureitemdetail.impl.common.ItemDetailEvent
import proton.android.pass.featureitemdetail.impl.common.ShareClickAction

sealed interface CreditCardDetailUiState {

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
        val isHistoryFeatureEnabled: Boolean,
        val isFileAttachmentsEnabled: Boolean,
        val attachments: List<Attachment>,
        val hasMoreThanOneVault: Boolean
    ) : CreditCardDetailUiState

    @Stable
    data class ItemContent(
        val model: ItemUiModel,
        val cardNumber: CardNumberState
    )

}
