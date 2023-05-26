package proton.android.pass.featureitemcreate.impl.alias

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.composecomponents.impl.dialogs.ConfirmCloseDialog
import proton.android.pass.composecomponents.impl.form.TitleVaultSelectionSection
import proton.android.pass.composecomponents.impl.keyboard.keyboardAsState
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.featureitemcreate.impl.R
import proton.android.pass.featureitemcreate.impl.alias.AliasItemValidationErrors.BlankTitle
import proton.android.pass.featureitemcreate.impl.login.ShareError.EmptyShareList
import proton.android.pass.featureitemcreate.impl.login.ShareError.SharesNotAvailable
import proton.android.pass.featureitemcreate.impl.login.ShareUiState
import proton.pass.domain.ShareId

private enum class CAActionAfterHideKeyboard {
    SelectVault
}

@OptIn(ExperimentalLifecycleComposeApi::class, ExperimentalComposeUiApi::class)
@Composable
fun CreateAliasScreen(
    modifier: Modifier = Modifier,
    selectVault: ShareId?,
    onNavigate: (CreateAliasNavigation) -> Unit,
    viewModel: CreateAliasViewModel = hiltViewModel()
) {
    LaunchedEffect(selectVault) {
        if (selectVault != null) {
            viewModel.changeVault(selectVault)
        }
    }

    val uiState by viewModel.createAliasUiState.collectAsStateWithLifecycle()
    val keyboardState by keyboardAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    var actionWhenKeyboardDisappears by remember { mutableStateOf<CAActionAfterHideKeyboard?>(null) }

    var showConfirmDialog by rememberSaveable { mutableStateOf(false) }
    val onExit = {
        if (uiState.baseAliasUiState.hasUserEditedContent) {
            showConfirmDialog = !showConfirmDialog
        } else {
            onNavigate(CreateAliasNavigation.Close)
        }
    }
    BackHandler {
        onExit()
    }

    LaunchedEffect(uiState.baseAliasUiState.closeScreenEvent) {
        if (uiState.baseAliasUiState.closeScreenEvent is CloseScreenEvent.Close) {
            onNavigate(CreateAliasNavigation.Close)
        }
    }
    val (showVaultSelector, selectedVault) = when (val shares = uiState.shareUiState) {
        ShareUiState.Loading,
        ShareUiState.NotInitialised -> false to null

        is ShareUiState.Error -> {
            if (shares.shareError == EmptyShareList || shares.shareError == SharesNotAvailable) {
                viewModel.onEmitSnackbarMessage(AliasSnackbarMessage.InitError)
                LaunchedEffect(Unit) {
                    onNavigate(CreateAliasNavigation.Close)
                }
            }
            false to null
        }

        is ShareUiState.Success -> (shares.vaultList.size > 1) to shares.currentVault
    }
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        AliasContent(
            uiState = uiState.baseAliasUiState,
            selectedShareId = selectedVault?.vault?.shareId,
            topBarActionName = stringResource(id = R.string.title_create_alias),
            isCreateMode = true,
            isEditAllowed = uiState.baseAliasUiState.isLoadingState == IsLoadingState.NotLoading,
            onUpClick = onExit,
            onAliasCreated = { shareId, itemId, alias ->
                val event = CreateAliasNavigation.Created(shareId, itemId, alias)
                onNavigate(event)
            },
            onSubmit = { shareId -> viewModel.createAlias(shareId) },
            onSuffixChange = { viewModel.onSuffixChange(it) },
            onMailboxesChanged = { viewModel.onMailboxesChanged(it) },
            onNoteChange = { viewModel.onNoteChange(it) },
            onPrefixChange = { viewModel.onPrefixChange(it) },
            onUpgrade = { onNavigate(CreateAliasNavigation.Upgrade) },
            titleSection = {
                TitleVaultSelectionSection(
                    titleValue = uiState.baseAliasUiState.aliasItem.title,
                    onTitleChanged = { viewModel.onTitleChange(it) },
                    onTitleRequiredError = uiState.baseAliasUiState.errorList.contains(BlankTitle),
                    enabled = uiState.baseAliasUiState.isLoadingState == IsLoadingState.NotLoading,
                    showVaultSelector = showVaultSelector,
                    vaultName = selectedVault?.vault?.name,
                    vaultIcon = selectedVault?.vault?.icon,
                    vaultColor = selectedVault?.vault?.color,
                    onVaultClicked = {
                        actionWhenKeyboardDisappears = CAActionAfterHideKeyboard.SelectVault
                        keyboardController?.hide()
                    }
                )
            }
        )

        ConfirmCloseDialog(
            show = showConfirmDialog,
            onCancel = {
                showConfirmDialog = false
            },
            onConfirm = {
                showConfirmDialog = false
                onNavigate(CreateAliasNavigation.Close)
            }
        )
    }

    LaunchedEffect(keyboardState, actionWhenKeyboardDisappears) {
        if (!keyboardState) {
            when (actionWhenKeyboardDisappears) {
                CAActionAfterHideKeyboard.SelectVault -> {
                    selectedVault ?: return@LaunchedEffect
                    onNavigate(CreateAliasNavigation.SelectVault(selectedVault.vault.shareId))
                    actionWhenKeyboardDisappears = null // Clear flag
                }

                null -> {}
            }
        }
    }
}
