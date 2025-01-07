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

package proton.android.pass.features.itemcreate.alias

import androidx.compose.runtime.Immutable
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.domain.ShareId
import proton.android.pass.features.itemcreate.ItemSavedState
import proton.android.pass.features.itemcreate.common.ShareUiState

@Immutable
data class BaseAliasUiState(
    val isDraft: Boolean,
    val errorList: Set<AliasItemValidationErrors>,
    val isLoadingState: IsLoadingState,
    val itemSavedState: ItemSavedState,
    val isAliasDraftSavedState: AliasDraftSavedState,
    val isApplyButtonEnabled: IsButtonEnabled,
    val closeScreenEvent: CloseScreenEvent,
    val hasUserEditedContent: Boolean,
    val hasReachedAliasLimit: Boolean,
    val canUpgrade: Boolean,
    val isAliasManagementEnabled: Boolean,
    val isFileAttachmentEnabled: Boolean,
    val displayFileAttachmentsOnboarding: Boolean,
    val attachmentsState: AttachmentsState
) {
    companion object {
        val Initial = BaseAliasUiState(
            isDraft = false,
            errorList = emptySet(),
            isLoadingState = IsLoadingState.Loading,
            itemSavedState = ItemSavedState.Unknown,
            isAliasDraftSavedState = AliasDraftSavedState.Unknown,
            isApplyButtonEnabled = IsButtonEnabled.Disabled,
            closeScreenEvent = CloseScreenEvent.NotClose,
            hasUserEditedContent = false,
            hasReachedAliasLimit = false,
            canUpgrade = false,
            isAliasManagementEnabled = false,
            isFileAttachmentEnabled = false,
            displayFileAttachmentsOnboarding = false,
            attachmentsState = AttachmentsState.Initial
        )
    }
}

@Immutable
data class CreateAliasUiState(
    val shareUiState: ShareUiState,
    val baseAliasUiState: BaseAliasUiState
) {
    companion object {
        val Initial = CreateAliasUiState(
            shareUiState = ShareUiState.NotInitialised,
            baseAliasUiState = BaseAliasUiState.Initial
        )
    }
}

@Immutable
data class UpdateAliasUiState(
    val selectedShareId: ShareId?,
    val canModify: Boolean,
    val baseAliasUiState: BaseAliasUiState
) {
    companion object {
        val Initial = UpdateAliasUiState(
            selectedShareId = null,
            canModify = false,
            baseAliasUiState = BaseAliasUiState.Initial
        )
    }
}
