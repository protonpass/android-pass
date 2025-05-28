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

package proton.android.pass.features.itemdetail.login

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList
import proton.android.pass.common.api.Option
import proton.android.pass.commonrust.api.PasswordScore
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.commonuimodels.api.UIPasskeyContent
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.data.api.usecases.ItemActions
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.Share
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole
import proton.android.pass.features.itemdetail.common.ItemDetailEvent
import proton.android.pass.features.itemdetail.common.LoginItemFeatures
import proton.android.pass.features.itemdetail.common.ShareClickAction

internal sealed interface LoginDetailUiState {

    @Stable
    data object NotInitialised : LoginDetailUiState

    @Stable
    data object Error : LoginDetailUiState

    @Stable
    data object Pending : LoginDetailUiState

    @Stable
    data class Success(
        val itemUiModel: ItemUiModel,
        val passwordScore: PasswordScore?,
        val share: Share,
        val totpUiState: TotpUiState?,
        val linkedAlias: Option<LinkedAliasItem>,
        val isLoading: Boolean,
        val isItemSentToTrash: Boolean,
        val isPermanentlyDeleted: Boolean,
        val isRestoredFromTrash: Boolean,
        val canPerformItemActions: Boolean,
        val customFields: ImmutableList<CustomFieldUiContent>,
        val passkeys: ImmutableList<UIPasskeyContent>,
        val shareClickAction: ShareClickAction,
        val itemActions: ItemActions,
        val event: ItemDetailEvent,
        val itemFeatures: LoginItemFeatures,
        val attachmentsState: AttachmentsState,
        val hasMoreThanOneVault: Boolean
    ) : LoginDetailUiState {

        private val isVaultShare: Boolean = share is Share.Vault

        internal val canViewItemHistory: Boolean = itemFeatures.isHistoryEnabled
            .and(share.shareRole !is ShareRole.Read)

        internal val canMigrate: Boolean = isVaultShare && itemActions.canMoveToOtherVault.value()

        internal val canMoveToTrash: Boolean = itemActions.canMoveToTrash

        internal val canLeaveItem: Boolean = !isVaultShare

        internal val shareSharedCount: Int = share.memberCount.plus(itemUiModel.shareCount)

    }
}

sealed interface TotpUiState {
    data object Hidden : TotpUiState
    data object Limited : TotpUiState

    @Stable
    data class Visible(
        val code: String,
        val remainingSeconds: Int,
        val totalSeconds: Int
    ) : TotpUiState
}

@Stable
data class LinkedAliasItem(
    val shareId: ShareId,
    val itemId: ItemId
)

@Stable
sealed interface CustomFieldUiContent {

    @Stable
    sealed interface Limited : CustomFieldUiContent {
        val label: String

        @Stable
        data class Text(override val label: String) : Limited

        @Stable
        data class Hidden(override val label: String) : Limited

        @Stable
        data class Totp(override val label: String) : Limited
    }

    @Stable
    data class Text(val label: String, val content: String) : CustomFieldUiContent

    @Stable
    data class Hidden(val label: String, val content: HiddenState) : CustomFieldUiContent

    @Stable
    data class Totp(
        val label: String,
        val code: String,
        val remainingSeconds: Int,
        val totalSeconds: Int
    ) : CustomFieldUiContent
}
