package me.proton.pass.presentation.create.note

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import me.proton.pass.common.api.None
import me.proton.pass.common.api.Some
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ShareId
import me.proton.pass.presentation.create.note.NoteItemValidationErrors.BlankTitle
import me.proton.pass.presentation.create.note.NoteSnackbarMessage.EmptyShareIdError
import me.proton.pass.presentation.uievents.IsLoadingState
import me.proton.pass.presentation.uievents.ItemSavedState

@ExperimentalComposeUiApi
@Composable
internal fun NoteContent(
    modifier: Modifier = Modifier,
    topBarTitle: String,
    topBarActionName: String,
    uiState: CreateUpdateNoteUiState,
    canDelete: Boolean,
    onUpClick: () -> Unit,
    onSuccess: (ShareId, ItemId) -> Unit,
    onSubmit: (ShareId) -> Unit,
    onTitleChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onEmitSnackbarMessage: (NoteSnackbarMessage) -> Unit,
    onDelete: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            NoteTopBar(
                shareId = uiState.shareId.value(),
                topBarActionName = topBarActionName,
                topBarTitle = topBarTitle,
                isLoadingState = uiState.isLoadingState,
                onUpClick = onUpClick,
                onEmitSnackbarMessage = onEmitSnackbarMessage,
                onSubmit = onSubmit
            )
        }
    ) { padding ->
        CreateNoteItemForm(
            modifier = Modifier.padding(padding),
            state = uiState.noteItem,
            onTitleRequiredError = uiState.errorList.contains(BlankTitle),
            onTitleChange = onTitleChange,
            onNoteChange = onNoteChange,
            enabled = uiState.isLoadingState != IsLoadingState.Loading,
            canDelete = canDelete,
            onDelete = onDelete
        )
        LaunchedEffect(uiState.isItemSaved is ItemSavedState.Success) {
            val isItemSaved = uiState.isItemSaved
            if (isItemSaved is ItemSavedState.Success) {
                when (uiState.shareId) {
                    None -> onEmitSnackbarMessage(EmptyShareIdError)
                    is Some -> onSuccess(uiState.shareId.value, isItemSaved.itemId)
                }
            }
        }
    }
}
