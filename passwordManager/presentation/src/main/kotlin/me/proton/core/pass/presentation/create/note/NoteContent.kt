package me.proton.core.pass.presentation.create.note

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.proton.android.pass.ui.shared.CrossBackIcon
import me.proton.android.pass.ui.shared.LoadingDialog
import me.proton.android.pass.ui.shared.TopBarTitleView
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.presentation.create.note.NoteItemValidationErrors.BlankTitle
import me.proton.core.pass.presentation.uievents.IsLoadingState
import me.proton.core.pass.presentation.uievents.ItemSavedState

@ExperimentalComposeUiApi
@Composable
internal fun NoteContent(
    @StringRes topBarTitle: Int,
    @StringRes topBarActionName: Int,
    uiState: CreateUpdateNoteUiState,
    onUpClick: () -> Unit,
    onSuccess: (ItemId) -> Unit,
    onSubmit: () -> Unit,
    onTitleChange: (String) -> Unit,
    onNoteChange: (String) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Scaffold(
        topBar = {
            ProtonTopAppBar(
                title = { TopBarTitleView(topBarTitle) },
                navigationIcon = { CrossBackIcon(onUpClick = onUpClick) },
                actions = {
                    IconButton(
                        onClick = {
                            keyboardController?.hide()
                            onSubmit()
                        },
                        modifier = Modifier.padding(end = 10.dp)
                    ) {
                        Text(
                            text = stringResource(topBarActionName),
                            color = ProtonTheme.colors.brandNorm,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.W500
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoadingState == IsLoadingState.Loading) {
            LoadingDialog()
        }
        CreateNoteItemForm(
            state = uiState.noteItem,
            modifier = Modifier.padding(padding),
            onTitleRequiredError = uiState.errorList.contains(BlankTitle),
            onTitleChange = onTitleChange,
            onNoteChange = onNoteChange
        )
        LaunchedEffect(uiState.isItemSaved is ItemSavedState.Success) {
            val isItemSaved = uiState.isItemSaved
            if (isItemSaved is ItemSavedState.Success) {
                onSuccess(isItemSaved.itemId)
            }
        }
    }
}
