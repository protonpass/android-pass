package me.proton.pass.presentation.create.note

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.proton.android.pass.ui.shared.CrossBackIcon
import me.proton.android.pass.ui.shared.TopBarTitleView
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.common.api.None
import me.proton.pass.common.api.Some
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ShareId
import me.proton.pass.presentation.components.common.TopBarCircularProgressIndicator
import me.proton.pass.presentation.create.note.NoteItemValidationErrors.BlankTitle
import me.proton.pass.presentation.create.note.NoteSnackbarMessage.EmptyShareIdError
import me.proton.pass.presentation.uievents.IsLoadingState
import me.proton.pass.presentation.uievents.ItemSavedState

@ExperimentalComposeUiApi
@Composable
internal fun NoteContent(
    modifier: Modifier = Modifier,
    @StringRes topBarTitle: Int,
    @StringRes topBarActionName: Int,
    uiState: CreateUpdateNoteUiState,
    onUpClick: () -> Unit,
    onSuccess: (ShareId, ItemId) -> Unit,
    onSubmit: (ShareId) -> Unit,
    onTitleChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onEmitSnackbarMessage: (NoteSnackbarMessage) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    Scaffold(
        modifier = modifier,
        topBar = {
            ProtonTopAppBar(
                title = { TopBarTitleView(topBarTitle) },
                navigationIcon = { CrossBackIcon(onUpClick = onUpClick) },
                actions = {
                    IconButton(
                        onClick = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            when (uiState.shareId) {
                                None -> onEmitSnackbarMessage(EmptyShareIdError)
                                is Some -> onSubmit(uiState.shareId.value)
                            }
                        },
                        modifier = Modifier.padding(end = 10.dp)
                    ) {
                        if (uiState.isLoadingState == IsLoadingState.Loading) {
                            TopBarCircularProgressIndicator()
                        } else {
                            Text(
                                text = stringResource(topBarActionName),
                                color = ProtonTheme.colors.brandNorm,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.W500
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        CreateNoteItemForm(
            state = uiState.noteItem,
            modifier = Modifier.padding(padding),
            onTitleRequiredError = uiState.errorList.contains(BlankTitle),
            onTitleChange = onTitleChange,
            onNoteChange = onNoteChange,
            enabled = uiState.isLoadingState != IsLoadingState.Loading
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
