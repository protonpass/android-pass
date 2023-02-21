package proton.android.pass.featurecreateitem.impl.alias

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import proton.android.pass.commonui.api.PassColors
import proton.android.pass.composecomponents.impl.bottomsheet.PassModalBottomSheetLayout
import proton.android.pass.featurecreateitem.impl.alias.AliasBottomSheetContentType.AliasOptions
import proton.android.pass.featurecreateitem.impl.alias.AliasBottomSheetContentType.VaultSelection
import proton.android.pass.featurecreateitem.impl.alias.AliasItemValidationErrors.BlankAlias
import proton.android.pass.featurecreateitem.impl.alias.AliasItemValidationErrors.BlankTitle
import proton.android.pass.featurecreateitem.impl.alias.AliasItemValidationErrors.InvalidAliasContent
import proton.android.pass.featurecreateitem.impl.alias.mailboxes.SelectMailboxesDialog
import proton.android.pass.featurecreateitem.impl.common.CreateUpdateTopBar
import proton.android.pass.featurecreateitem.impl.login.bottomsheet.VaultSelectionBottomSheet
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
@Suppress("LongParameterList", "LongMethod")
internal fun AliasContent(
    modifier: Modifier = Modifier,
    uiState: CreateUpdateAliasUiState,
    topBarActionName: String,
    canEdit: Boolean,
    isUpdate: Boolean,
    isEditAllowed: Boolean,
    onUpClick: () -> Unit,
    onSubmit: (ShareId) -> Unit,
    onAliasCreated: (ShareId, ItemId, String) -> Unit,
    onAliasDraftCreated: (ShareId, AliasItem) -> Unit,
    onSuffixChange: (AliasSuffixUiModel) -> Unit,
    onMailboxesChanged: (List<SelectedAliasMailboxUiModel>) -> Unit,
    onTitleChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onAliasChange: (String) -> Unit,
    onVaultSelect: (ShareId) -> Unit
) {
    val scope = rememberCoroutineScope()

    val bottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden
    )
    var currentBottomSheet by remember { mutableStateOf(AliasOptions) }

    // If the BottomSheet is visible and the user presses back, dismiss the BottomSheet
    BackHandler(enabled = bottomSheetState.isVisible) {
        scope.launch { bottomSheetState.hide() }
    }

    var showMailboxDialog by remember { mutableStateOf(false) }

    PassModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetContent = {
            when (currentBottomSheet) {
                AliasOptions -> AliasBottomSheetContents(
                    modelState = uiState.aliasItem,
                    onSuffixSelect = { suffix ->
                        scope.launch {
                            bottomSheetState.hide()
                            onSuffixChange(suffix)
                        }
                    }
                )
                VaultSelection -> VaultSelectionBottomSheet(
                    shareList = uiState.shareList,
                    selectedShare = uiState.selectedShareId!!,
                    onVaultClick = {
                        onVaultSelect(it)
                        scope.launch {
                            bottomSheetState.hide()
                        }
                    }
                )
            }

        }
    ) {
        Scaffold(
            modifier = modifier,
            topBar = {
                CreateUpdateTopBar(
                    text = topBarActionName,
                    isLoading = uiState.isLoadingState.value(),
                    color = PassColors.GreenAccent,
                    onCloseClick = onUpClick,
                    onActionClick = { uiState.selectedShareId?.id?.let(onSubmit) }
                )
            }
        ) { padding ->
            CreateAliasForm(
                aliasItem = uiState.aliasItem,
                selectedShare = uiState.selectedShareId,
                canEdit = canEdit,
                isUpdate = isUpdate,
                isEditAllowed = isEditAllowed,
                modifier = Modifier.padding(padding),
                onTitleRequiredError = uiState.errorList.contains(BlankTitle),
                onAliasRequiredError = uiState.errorList.contains(BlankAlias),
                onInvalidAliasError = uiState.errorList.contains(InvalidAliasContent),
                onSuffixClick = {
                    scope.launch {
                        if (canEdit) {
                            currentBottomSheet = AliasOptions
                            bottomSheetState.show()
                        }
                    }
                },
                onMailboxClick = {
                    scope.launch {
                        showMailboxDialog = true
                    }
                },
                onTitleChange = { onTitleChange(it) },
                onNoteChange = { onNoteChange(it) },
                onAliasChange = { onAliasChange(it) },
                onVaultSelectorClick = {
                    scope.launch {
                        currentBottomSheet = VaultSelection
                        bottomSheetState.show()
                    }
                }
            )

            SelectMailboxesDialog(
                show = showMailboxDialog,
                mailboxes = uiState.aliasItem.mailboxes,
                onMailboxesChanged = {
                    showMailboxDialog = false
                    onMailboxesChanged(it)
                },
                onDismiss = { showMailboxDialog = false }
            )
            IsAliasSavedLaunchedEffect(uiState, onAliasCreated)
            IsAliasDraftSavedLaunchedEffect(uiState, onAliasDraftCreated)
        }
    }
}

@Composable
private fun IsAliasDraftSavedLaunchedEffect(
    uiState: CreateUpdateAliasUiState,
    onAliasDraftCreated: (ShareId, AliasItem) -> Unit
) {
    val isAliasDraftSaved = uiState.isAliasDraftSavedState
    if (isAliasDraftSaved is AliasDraftSavedState.Success) {
        LaunchedEffect(uiState.selectedShareId) {
            uiState.selectedShareId?.let {
                onAliasDraftCreated(it.id, isAliasDraftSaved.aliasItem)
            }
        }
    }
}

@Composable
private fun IsAliasSavedLaunchedEffect(
    uiState: CreateUpdateAliasUiState,
    onAliasCreated: (ShareId, ItemId, String) -> Unit
) {
    val isAliasSaved = uiState.isAliasSavedState
    if (isAliasSaved is AliasSavedState.Success) {
        LaunchedEffect(uiState.selectedShareId) {
            uiState.selectedShareId?.let {
                onAliasCreated(it.id, isAliasSaved.itemId, isAliasSaved.alias)
            }
        }
    }
}
