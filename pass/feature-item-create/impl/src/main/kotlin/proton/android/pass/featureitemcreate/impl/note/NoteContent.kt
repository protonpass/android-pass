package proton.android.pass.featureitemcreate.impl.note

import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.bottomsheet.PassModalBottomSheetLayout
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.featureitemcreate.impl.common.CreateUpdateTopBar
import proton.android.pass.featureitemcreate.impl.login.bottomsheet.VaultSelectionBottomSheet
import proton.android.pass.featureitemcreate.impl.note.NoteItemValidationErrors.BlankTitle
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

@OptIn(ExperimentalMaterialApi::class)
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
    onVaultSelect: (ShareId) -> Unit
) {
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    PassModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetContent = {
            VaultSelectionBottomSheet(
                shareList = uiState.shareList,
                selectedShare = uiState.selectedShareId,
                onVaultClick = {
                    onVaultSelect(it)
                    scope.launch {
                        bottomSheetState.hide()
                    }
                }
            )
        }
    ) {
        Scaffold(
            modifier = modifier,
            topBar = {
                CreateUpdateTopBar(
                    text = topBarActionName,
                    isLoading = uiState.isLoadingState.value(),
                    opaqueColor = PassTheme.colors.accentYellowOpaque,
                    weakestColor = PassTheme.colors.accentYellowWeakest,
                    onCloseClick = onUpClick,
                    onActionClick = { uiState.selectedShareId?.id?.let(onSubmit) }
                )
            }
        ) { padding ->
            CreateNoteItemForm(
                modifier = Modifier.padding(padding),
                noteItem = uiState.noteItem,
                selectedShare = uiState.selectedShareId,
                onTitleRequiredError = uiState.errorList.contains(BlankTitle),
                onTitleChange = onTitleChange,
                onNoteChange = onNoteChange,
                enabled = uiState.isLoadingState != IsLoadingState.Loading,
                showVaultSelector = showVaultSelector,
                onVaultSelectorClick = {
                    scope.launch {
                        bottomSheetState.show()
                    }
                }
            )
            LaunchedEffect(uiState.isItemSaved is ItemSavedState.Success) {
                val isItemSaved = uiState.isItemSaved
                if (isItemSaved is ItemSavedState.Success && uiState.selectedShareId != null) {
                    onSuccess(
                        uiState.selectedShareId.id,
                        isItemSaved.itemId
                    )
                }
            }
        }
    }
}
