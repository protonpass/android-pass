package proton.android.pass.featureitemcreate.impl.alias

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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.bottomsheet.PassModalBottomSheetLayout
import proton.android.pass.featureitemcreate.impl.alias.AliasBottomSheetContentType.AliasOptions
import proton.android.pass.featureitemcreate.impl.alias.AliasBottomSheetContentType.VaultSelection
import proton.android.pass.featureitemcreate.impl.alias.AliasItemValidationErrors.BlankPrefix
import proton.android.pass.featureitemcreate.impl.alias.AliasItemValidationErrors.BlankTitle
import proton.android.pass.featureitemcreate.impl.alias.AliasItemValidationErrors.InvalidAliasContent
import proton.android.pass.featureitemcreate.impl.alias.mailboxes.SelectMailboxesDialog
import proton.android.pass.featureitemcreate.impl.alias.saver.AliasBottomSheetContentTypeSaver
import proton.android.pass.featureitemcreate.impl.alias.suffixes.SelectSuffixDialog
import proton.android.pass.featureitemcreate.impl.common.CreateUpdateTopBar
import proton.android.pass.featureitemcreate.impl.login.bottomsheet.VaultSelectionBottomSheet
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
@Suppress("LongParameterList", "LongMethod")
internal fun AliasContent(
    modifier: Modifier = Modifier,
    uiState: CreateUpdateAliasUiState,
    topBarActionName: String,
    isCreateMode: Boolean,
    isEditAllowed: Boolean,
    showVaultSelector: Boolean,
    onUpClick: () -> Unit,
    onSubmit: (ShareId) -> Unit,
    onAliasCreated: (ShareId, ItemId, String) -> Unit,
    onSuffixChange: (AliasSuffixUiModel) -> Unit,
    onMailboxesChanged: (List<SelectedAliasMailboxUiModel>) -> Unit,
    onTitleChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onPrefixChange: (String) -> Unit,
    onVaultSelect: (ShareId) -> Unit
) {
    val scope = rememberCoroutineScope()

    val bottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden
    )
    var currentBottomSheet by rememberSaveable(stateSaver = AliasBottomSheetContentTypeSaver) {
        mutableStateOf(AliasOptions)
    }

    // If the BottomSheet is visible and the user presses back, dismiss the BottomSheet
    BackHandler(enabled = bottomSheetState.isVisible) {
        scope.launch { bottomSheetState.hide() }
    }

    var showMailboxDialog by rememberSaveable { mutableStateOf(false) }
    var showSuffixDialog by rememberSaveable { mutableStateOf(false) }

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
                    shareList = uiState.vaultList,
                    selectedShareId = uiState.selectedVault?.vault?.shareId,
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
                    actionColor = PassTheme.colors.aliasInteractionNorm,
                    iconColor = PassTheme.colors.aliasInteractionNormMajor2,
                    iconBackgroundColor = PassTheme.colors.aliasInteractionNormMinor1,
                    onCloseClick = onUpClick,
                    onActionClick = { uiState.selectedVault?.vault?.shareId?.let(onSubmit) }
                )
            }
        ) { padding ->
            CreateAliasForm(
                modifier = Modifier.padding(padding),
                aliasItem = uiState.aliasItem,
                selectedVault = uiState.selectedVault,
                isCreateMode = isCreateMode,
                isEditAllowed = isEditAllowed,
                showVaultSelector = showVaultSelector,
                isLoading = uiState.isLoadingState.value(),
                onTitleRequiredError = uiState.errorList.contains(BlankTitle),
                onAliasRequiredError = uiState.errorList.contains(BlankPrefix),
                onInvalidAliasError = uiState.errorList.contains(InvalidAliasContent),
                onSuffixClick = {
                    scope.launch {
                        showSuffixDialog = true
                    }
                },
                onMailboxClick = {
                    scope.launch {
                        showMailboxDialog = true
                    }
                },
                onTitleChange = { onTitleChange(it) },
                onNoteChange = { onNoteChange(it) },
                onPrefixChange = { onPrefixChange(it) },
                onVaultSelectorClick = {
                    scope.launch {
                        currentBottomSheet = VaultSelection
                        bottomSheetState.show()
                    }
                }
            )

            SelectSuffixDialog(
                show = showSuffixDialog,
                suffixes = uiState.aliasItem.aliasOptions.suffixes.toImmutableList(),
                selectedSuffix = uiState.aliasItem.selectedSuffix,
                color = PassTheme.colors.aliasInteractionNorm,
                onSuffixChanged = { suffix ->
                    scope.launch {
                        showSuffixDialog = false
                        onSuffixChange(suffix)
                    }
                },
                onDismiss = {
                    scope.launch {
                        showSuffixDialog = false
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
        LaunchedEffect(uiState.selectedVault) {
            uiState.selectedVault?.let {
                onAliasCreated(it.vault.shareId, isAliasSaved.itemId, isAliasSaved.alias)
            }
        }
    }
}
