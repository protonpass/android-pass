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
import proton.android.pass.featureitemcreate.impl.alias.AliasItemValidationErrors.BlankPrefix
import proton.android.pass.featureitemcreate.impl.alias.AliasItemValidationErrors.BlankTitle
import proton.android.pass.featureitemcreate.impl.alias.AliasItemValidationErrors.InvalidAliasContent
import proton.android.pass.featureitemcreate.impl.alias.mailboxes.SelectMailboxesDialog
import proton.android.pass.featureitemcreate.impl.alias.suffixes.SelectSuffixDialog
import proton.android.pass.featureitemcreate.impl.common.CreateUpdateTopBar
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
    onSelectVaultClick: () -> Unit,
    onUpgrade: () -> Unit
) {
    val scope = rememberCoroutineScope()

    val bottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden
    )

    // If the BottomSheet is visible and the user presses back, dismiss the BottomSheet
    BackHandler(enabled = bottomSheetState.isVisible) {
        scope.launch { bottomSheetState.hide() }
    }

    var showMailboxDialog by rememberSaveable { mutableStateOf(false) }
    var showSuffixDialog by rememberSaveable { mutableStateOf(false) }

    PassModalBottomSheetLayout(
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
                CreateUpdateTopBar(
                    text = topBarActionName,
                    isLoading = uiState.isLoadingState.value(),
                    actionColor = PassTheme.colors.aliasInteractionNormMajor1,
                    iconColor = PassTheme.colors.aliasInteractionNormMajor2,
                    iconBackgroundColor = PassTheme.colors.aliasInteractionNormMinor1,
                    onCloseClick = onUpClick,
                    showUpgrade = uiState.hasReachedAliasLimit,
                    onActionClick = { uiState.selectedVault?.vault?.shareId?.let(onSubmit) },
                    onUpgrade = onUpgrade
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
                showUpgrade = uiState.hasReachedAliasLimit,
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
                onVaultSelectorClick = onSelectVaultClick
            )

            SelectSuffixDialog(
                show = showSuffixDialog,
                canUpgrade = uiState.canUpgrade,
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
                },
                onUpgrade = onUpgrade
            )

            if (showMailboxDialog && uiState.aliasItem.mailboxes.isNotEmpty()) {
                SelectMailboxesDialog(
                    mailboxes = uiState.aliasItem.mailboxes,
                    color = PassTheme.colors.aliasInteractionNorm,
                    canUpgrade = uiState.canUpgrade,
                    onMailboxesChanged = {
                        showMailboxDialog = false
                        onMailboxesChanged(it)
                    },
                    onDismiss = { showMailboxDialog = false },
                    onUpgrade = onUpgrade
                )
            }

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
