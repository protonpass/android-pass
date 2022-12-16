package me.proton.pass.presentation.create.alias

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import me.proton.core.compose.component.ProtonModalBottomSheetLayout
import me.proton.pass.common.api.None
import me.proton.pass.common.api.Some
import me.proton.pass.domain.AliasSuffix
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ShareId
import me.proton.pass.presentation.create.alias.AliasItemValidationErrors.BlankAlias
import me.proton.pass.presentation.create.alias.AliasItemValidationErrors.BlankTitle
import me.proton.pass.presentation.create.alias.AliasItemValidationErrors.InvalidAliasContent
import me.proton.pass.presentation.create.alias.AliasSnackbarMessage.EmptyShareIdError
import me.proton.pass.presentation.create.alias.mailboxes.SelectMailboxesDialog
import me.proton.pass.presentation.uievents.AliasDraftSavedState
import me.proton.pass.presentation.uievents.AliasSavedState

@OptIn(ExperimentalMaterialApi::class)
@Composable
@Suppress("LongParameterList", "LongMethod")
internal fun AliasContent(
    modifier: Modifier = Modifier,
    uiState: CreateUpdateAliasUiState,
    @StringRes topBarTitle: Int,
    canEdit: Boolean,
    canDelete: Boolean,
    isEditAllowed: Boolean,
    onUpClick: () -> Unit,
    onSubmit: (ShareId) -> Unit,
    onAliasCreated: (ShareId, ItemId, String) -> Unit,
    onAliasDraftCreated: (ShareId, AliasItem) -> Unit,
    onSuffixChange: (AliasSuffix) -> Unit,
    onMailboxesChanged: (List<AliasMailboxUiModel>) -> Unit,
    onTitleChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onAliasChange: (String) -> Unit,
    onEmitSnackbarMessage: (AliasSnackbarMessage) -> Unit,
    onDeleteAlias: () -> Unit
) {
    val scope = rememberCoroutineScope()

    val bottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        confirmStateChange = { false }
    )

    // If the BottomSheet is visible and the user presses back, dismiss the BottomSheet
    BackHandler(enabled = bottomSheetState.isVisible) {
        scope.launch { bottomSheetState.hide() }
    }

    val (showMailboxDialog, setShowMailboxDialog) = remember { mutableStateOf(false) }

    ProtonModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetContent = {
            AliasBottomSheetContents(
                modelState = uiState.aliasItem,
                onSuffixSelect = { suffix ->
                    scope.launch {
                        bottomSheetState.hide()
                        onSuffixChange(suffix)
                    }
                }
            )
        }
    ) {
        Scaffold(
            modifier = modifier,
            topBar = {
                AliasTopBar(
                    topBarTitle = topBarTitle,
                    onUpClick = onUpClick,
                    isDraft = uiState.isDraft,
                    isButtonEnabled = uiState.isApplyButtonEnabled,
                    shareId = uiState.shareId,
                    isLoadingState = uiState.isLoadingState,
                    onEmitSnackbarMessage = onEmitSnackbarMessage,
                    onSubmit = onSubmit
                )
            }
        ) { padding ->
            CreateAliasForm(
                state = uiState.aliasItem,
                canEdit = canEdit,
                canDelete = canDelete,
                isEditAllowed = isEditAllowed,
                modifier = Modifier.padding(padding),
                onTitleRequiredError = uiState.errorList.contains(BlankTitle),
                onAliasRequiredError = uiState.errorList.contains(BlankAlias),
                onInvalidAliasError = uiState.errorList.contains(InvalidAliasContent),
                onSuffixClick = {
                    scope.launch {
                        if (canEdit) {
                            bottomSheetState.show()
                        }
                    }
                },
                onMailboxClick = {
                    scope.launch {
                        setShowMailboxDialog(true)
                    }
                },
                onTitleChange = { onTitleChange(it) },
                onNoteChange = { onNoteChange(it) },
                onAliasChange = { onAliasChange(it) },
                onDeleteAliasClick = onDeleteAlias
            )

            SelectMailboxesDialog(
                show = showMailboxDialog,
                mailboxes = uiState.aliasItem.mailboxes,
                onMailboxesChanged = {
                    setShowMailboxDialog(false)
                    onMailboxesChanged(it)
                },
                onDismiss = {
                    setShowMailboxDialog(false)
                }
            )

            IsAliasSavedLaunchedEffect(uiState, onEmitSnackbarMessage, onAliasCreated)
            IsAliasDraftSavedLaunchedEffect(uiState, onEmitSnackbarMessage, onAliasDraftCreated)
        }
    }
}

@Composable
private fun IsAliasDraftSavedLaunchedEffect(
    uiState: CreateUpdateAliasUiState,
    onEmitSnackbarMessage: (AliasSnackbarMessage) -> Unit,
    onAliasDraftCreated: (ShareId, AliasItem) -> Unit
) {
    val isAliasDraftSaved = uiState.isAliasDraftSavedState
    if (isAliasDraftSaved is AliasDraftSavedState.Success) {
        LaunchedEffect(Unit) {
            when (uiState.shareId) {
                None -> onEmitSnackbarMessage(EmptyShareIdError)
                is Some -> onAliasDraftCreated(
                    uiState.shareId.value,
                    isAliasDraftSaved.aliasItem
                )
            }
        }
    }
}

@Composable
private fun IsAliasSavedLaunchedEffect(
    uiState: CreateUpdateAliasUiState,
    onEmitSnackbarMessage: (AliasSnackbarMessage) -> Unit,
    onAliasCreated: (ShareId, ItemId, String) -> Unit
) {
    val isAliasSaved = uiState.isAliasSavedState
    if (isAliasSaved is AliasSavedState.Success) {
        LaunchedEffect(Unit) {
            when (uiState.shareId) {
                None -> onEmitSnackbarMessage(EmptyShareIdError)
                is Some -> onAliasCreated(
                    uiState.shareId.value,
                    isAliasSaved.itemId,
                    isAliasSaved.alias
                )
            }
        }
    }
}
