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

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault
import proton.android.pass.features.itemcreate.common.CommonFieldValidationError
import proton.android.pass.features.itemcreate.common.CreateUpdateTopBar
import proton.android.pass.features.itemcreate.common.CustomFieldValidationError

@ExperimentalComposeUiApi
@Composable
internal fun NoteContent(
    modifier: Modifier = Modifier,
    topBarActionName: String,
    uiState: BaseNoteUiState,
    noteItemFormState: NoteItemFormState,
    selectedVault: Vault?,
    showVaultSelector: Boolean,
    selectedShareId: ShareId?,
    onEvent: (NoteContentUiEvent) -> Unit
) {
    Scaffold(
        modifier = modifier.systemBarsPadding(),
        topBar = {
            CreateUpdateTopBar(
                text = topBarActionName,
                isLoading = uiState.isLoadingState.value() ||
                    uiState.attachmentsState.loadingDraftAttachments.isNotEmpty(),
                actionColor = PassTheme.colors.noteInteractionNormMajor1,
                iconColor = PassTheme.colors.noteInteractionNormMajor2,
                iconBackgroundColor = PassTheme.colors.noteInteractionNormMinor1,
                selectedVault = selectedVault,
                showVaultSelector = showVaultSelector,
                onCloseClick = { onEvent(NoteContentUiEvent.Back) },
                onActionClick = {
                    selectedShareId ?: return@CreateUpdateTopBar
                    onEvent(NoteContentUiEvent.Submit(selectedShareId))
                },
                onUpgrade = {},
                onVaultSelectorClick = {
                    selectedShareId ?: return@CreateUpdateTopBar
                    onEvent(NoteContentUiEvent.OnVaultSelect(selectedShareId))
                }
            )
        }
    ) { padding ->
        NoteItemForm(
            modifier = Modifier.padding(padding),
            noteItemFormState = noteItemFormState,
            focusedField = uiState.focusedField,
            canUseCustomFields = uiState.canPerformPaidAction,
            attachmentsState = uiState.attachmentsState,
            displayFileAttachmentsOnboarding = uiState.displayFileAttachmentsOnboarding,
            customFieldValidationErrors = uiState.errorList.filterIsInstance<CustomFieldValidationError>(),
            onTitleRequiredError = uiState.errorList.contains(CommonFieldValidationError.BlankTitle),
            enabled = uiState.isLoadingState != IsLoadingState.Loading,
            onEvent = onEvent
        )
    }
}
