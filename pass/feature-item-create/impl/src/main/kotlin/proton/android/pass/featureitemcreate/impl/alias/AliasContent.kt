package proton.android.pass.featureitemcreate.impl.alias

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
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
import proton.android.pass.featureitemcreate.impl.alias.AliasItemValidationErrors.BlankPrefix
import proton.android.pass.featureitemcreate.impl.alias.AliasItemValidationErrors.InvalidAliasContent
import proton.android.pass.featureitemcreate.impl.alias.mailboxes.SelectMailboxesDialog
import proton.android.pass.featureitemcreate.impl.alias.suffixes.SelectSuffixDialog
import proton.android.pass.featureitemcreate.impl.common.CreateUpdateTopBar
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

@OptIn(ExperimentalComposeUiApi::class)
@Composable
@Suppress("LongParameterList", "LongMethod")
internal fun AliasContent(
    modifier: Modifier = Modifier,
    uiState: BaseAliasUiState,
    selectedShareId: ShareId?,
    topBarActionName: String,
    isCreateMode: Boolean,
    isEditAllowed: Boolean,
    onUpClick: () -> Unit,
    onSubmit: (ShareId) -> Unit,
    onAliasCreated: (ShareId, ItemId, String) -> Unit,
    onSuffixChange: (AliasSuffixUiModel) -> Unit,
    onMailboxesChanged: (List<SelectedAliasMailboxUiModel>) -> Unit,
    onNoteChange: (String) -> Unit,
    onPrefixChange: (String) -> Unit,
    onUpgrade: () -> Unit,
    titleSection: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()

    var showMailboxDialog by rememberSaveable { mutableStateOf(false) }
    var showSuffixDialog by rememberSaveable { mutableStateOf(false) }

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
                onActionClick = { selectedShareId?.let(onSubmit) },
                onUpgrade = onUpgrade
            )
        }
    ) { padding ->
        CreateAliasForm(
            modifier = Modifier.padding(padding),
            aliasItem = uiState.aliasItem,
            isCreateMode = isCreateMode,
            isEditAllowed = isEditAllowed,
            isLoading = uiState.isLoadingState.value(),
            showUpgrade = uiState.hasReachedAliasLimit,
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
            onNoteChange = { onNoteChange(it) },
            onPrefixChange = { onPrefixChange(it) },
            titleSection = titleSection
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

        IsAliasSavedLaunchedEffect(uiState.isAliasSavedState, selectedShareId, onAliasCreated)
    }
}

@Composable
private fun IsAliasSavedLaunchedEffect(
    aliasSavedState: AliasSavedState,
    selectedShareId: ShareId?,
    onAliasCreated: (ShareId, ItemId, String) -> Unit
) {
    if (aliasSavedState is AliasSavedState.Success) {
        LaunchedEffect(selectedShareId) {
            selectedShareId?.let {
                onAliasCreated(it, aliasSavedState.itemId, aliasSavedState.alias)
            }
        }
    }
}
