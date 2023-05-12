package proton.android.pass.featureitemcreate.impl.note

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.featureitemcreate.impl.common.CreateUpdateTopBar
import proton.android.pass.featureitemcreate.impl.note.NoteItemValidationErrors.BlankTitle
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

@ExperimentalComposeUiApi
@Composable
internal fun NoteContent(
    modifier: Modifier = Modifier,
    topBarActionName: String,
    uiState: CreateUpdateNoteUiState,
    showVaultSelector: Boolean,
    onUpClick: () -> Unit,
    onSuccess: (ShareId, ItemId) -> Unit,
    onSubmit: (ShareId) -> Unit,
    onTitleChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSelectVault: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CreateUpdateTopBar(
                text = topBarActionName,
                isLoading = uiState.isLoadingState.value(),
                actionColor = PassTheme.colors.noteInteractionNormMajor1,
                iconColor = PassTheme.colors.noteInteractionNormMajor2,
                iconBackgroundColor = PassTheme.colors.noteInteractionNormMinor1,
                onCloseClick = onUpClick,
                onActionClick = { uiState.selectedVault?.vault?.shareId?.let(onSubmit) },
                onUpgrade = {}
            )
        }
    ) { padding ->
        CreateNoteItemForm(
            modifier = Modifier.padding(padding),
            noteItem = uiState.noteItem,
            selectedVault = uiState.selectedVault,
            onTitleRequiredError = uiState.errorList.contains(BlankTitle),
            onTitleChange = onTitleChange,
            onNoteChange = onNoteChange,
            enabled = uiState.isLoadingState != IsLoadingState.Loading,
            showVaultSelector = showVaultSelector,
            onVaultSelectorClick = onSelectVault
        )
        LaunchedEffect(uiState.isItemSaved is ItemSavedState.Success) {
            val isItemSaved = uiState.isItemSaved
            if (isItemSaved is ItemSavedState.Success && uiState.selectedVault != null) {
                onSuccess(
                    uiState.selectedVault.vault.shareId,
                    isItemSaved.itemId
                )
            }
        }
    }
}
