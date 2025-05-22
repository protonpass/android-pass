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

package proton.android.pass.features.itemcreate.note

import androidx.compose.runtime.Immutable
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.domain.ShareId
import proton.android.pass.features.itemcreate.ItemSavedState
import proton.android.pass.features.itemcreate.common.ShareUiState

@Immutable
data class BaseNoteUiState(
    val errorList: Set<NoteItemValidationErrors>,
    val isLoadingState: IsLoadingState,
    val itemSavedState: ItemSavedState,
    val hasUserEditedContent: Boolean,
    val isFileAttachmentsEnabled: Boolean,
    val isCustomItemEnabled: Boolean,
    val displayFileAttachmentsOnboarding: Boolean,
    val attachmentsState: AttachmentsState
) {

    val canShowAttachments: Boolean = isFileAttachmentsEnabled &&
        !isCustomItemEnabled &&
        attachmentsState.canShowAttachmentSection(isDetail = false)

    companion object {
        val Initial = BaseNoteUiState(
            errorList = emptySet(),
            isLoadingState = IsLoadingState.NotLoading,
            itemSavedState = ItemSavedState.Unknown,
            hasUserEditedContent = false,
            isFileAttachmentsEnabled = false,
            isCustomItemEnabled = false,
            displayFileAttachmentsOnboarding = false,
            attachmentsState = AttachmentsState.Initial
        )
    }
}

@Immutable
data class CreateNoteUiState(
    val shareUiState: ShareUiState,
    val baseNoteUiState: BaseNoteUiState
) {
    companion object {
        val Initial = CreateNoteUiState(
            shareUiState = ShareUiState.NotInitialised,
            baseNoteUiState = BaseNoteUiState.Initial
        )
    }
}

@Immutable
data class UpdateNoteUiState(
    val selectedShareId: ShareId?,
    val baseNoteUiState: BaseNoteUiState
) {
    companion object {
        val Initial = UpdateNoteUiState(
            selectedShareId = null,
            baseNoteUiState = BaseNoteUiState.Initial
        )
    }
}
