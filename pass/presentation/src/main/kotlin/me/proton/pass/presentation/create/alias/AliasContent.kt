package me.proton.pass.presentation.create.alias

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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import me.proton.pass.presentation.components.common.LoadingDialog
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
import me.proton.pass.presentation.uievents.AliasDraftSavedState
import me.proton.pass.presentation.uievents.AliasSavedState
import me.proton.pass.presentation.uievents.IsLoadingState

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
@Suppress("LongParameterList", "LongMethod")
internal fun AliasContent(
    modifier: Modifier = Modifier,
    uiState: CreateUpdateAliasUiState,
    @StringRes topBarTitle: Int,
    canEdit: Boolean,
    canDelete: Boolean,
    onUpClick: () -> Unit,
    onSubmit: (ShareId) -> Unit,
    onAliasCreated: (ShareId, ItemId, String) -> Unit,
    onAliasDraftCreated: (ShareId, AliasItem) -> Unit,
    onSuffixChange: (AliasSuffix) -> Unit,
    onMailboxChange: (AliasMailboxUiModel) -> Unit,
    onTitleChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onAliasChange: (String) -> Unit,
    onEmitSnackbarMessage: (AliasSnackbarMessage) -> Unit,
    onDeleteAlias: () -> Unit
) {
    val scope = rememberCoroutineScope()

    val (bottomSheetContentType, setBottomSheetContentType) = remember {
        mutableStateOf<AliasBottomSheetType>(AliasBottomSheetType.Suffix)
    }

    val bottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        confirmStateChange = { false }
    )

    ProtonModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetContent = {
            AliasBottomSheetContents(
                modelState = uiState.aliasItem,
                contentType = bottomSheetContentType,
                onSuffixSelect = { suffix ->
                    scope.launch {
                        bottomSheetState.hide()
                        onSuffixChange(suffix)
                    }
                },
                onMailboxSelect = { mailbox -> scope.launch { onMailboxChange(mailbox) } },
                onCloseBottomSheet = { scope.launch { bottomSheetState.hide() } }
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
                    onEmitSnackbarMessage = onEmitSnackbarMessage,
                    onSubmit = onSubmit
                )
            }
        ) { padding ->
            if (uiState.isLoadingState == IsLoadingState.Loading) {
                LoadingDialog()
            }

            CreateAliasForm(
                state = uiState.aliasItem,
                canEdit = canEdit,
                canDelete = canDelete,
                modifier = Modifier.padding(padding),
                onTitleRequiredError = uiState.errorList.contains(BlankTitle),
                onAliasRequiredError = uiState.errorList.contains(BlankAlias),
                onInvalidAliasError = uiState.errorList.contains(InvalidAliasContent),
                onSuffixClick = {
                    scope.launch {
                        if (canEdit) {
                            setBottomSheetContentType(AliasBottomSheetType.Suffix)
                            bottomSheetState.show()
                        }
                    }
                },
                onMailboxClick = {
                    scope.launch {
                        setBottomSheetContentType(AliasBottomSheetType.Mailbox)
                        bottomSheetState.show()
                    }
                },
                onTitleChange = { onTitleChange(it) },
                onNoteChange = { onNoteChange(it) },
                onAliasChange = { onAliasChange(it) },
                onDeleteAliasClick = onDeleteAlias
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
