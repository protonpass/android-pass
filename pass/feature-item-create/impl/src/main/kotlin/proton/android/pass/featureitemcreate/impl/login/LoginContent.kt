package proton.android.pass.featureitemcreate.impl.login

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.some
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.commonuimodels.api.PackageInfoUi
import proton.android.pass.composecomponents.impl.bottomsheet.PassModalBottomSheetLayout
import proton.android.pass.composecomponents.impl.keyboard.keyboardAsState
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.featureitemcreate.impl.alias.saver.LoginBottomSheetContentTypeSaver
import proton.android.pass.featureitemcreate.impl.common.CreateUpdateTopBar
import proton.android.pass.featureitemcreate.impl.login.bottomsheet.AliasOptionsBottomSheet
import proton.android.pass.featureitemcreate.impl.login.bottomsheet.LoginBottomSheetContentType
import proton.android.pass.featureitemcreate.impl.login.bottomsheet.VaultSelectionBottomSheet
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

private enum class ActionAfterHideKeyboard {
    ShowBottomSheet,
    GeneratePassword,
    CreateAlias
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
internal fun LoginContent(
    modifier: Modifier = Modifier,
    topBarActionName: String,
    uiState: CreateUpdateLoginUiState,
    showCreateAliasButton: Boolean,
    isUpdate: Boolean,
    showVaultSelector: Boolean,
    onUpClick: () -> Unit,
    onSuccess: (ShareId, ItemId, ItemUiModel) -> Unit,
    onSubmit: (ShareId) -> Unit,
    onTitleChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onWebsiteChange: OnWebsiteChange,
    onNoteChange: (String) -> Unit,
    onTotpChange: (String) -> Unit,
    onRemoveAliasClick: () -> Unit,
    onVaultSelect: (ShareId) -> Unit,
    onPasteTotpClick: () -> Unit,
    onScanTotpClick: () -> Unit,
    onLinkedAppDelete: (PackageInfoUi) -> Unit,
    onCreateAlias: (ShareId, Option<String>) -> Unit,
    onGeneratePasswordClick: () -> Unit,
    onUpgrade: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )

    BackHandler {
        if (bottomSheetState.isVisible) {
            scope.launch {
                bottomSheetState.hide()
            }
        } else {
            onUpClick()
        }
    }

    var currentBottomSheet by rememberSaveable(stateSaver = LoginBottomSheetContentTypeSaver) {
        mutableStateOf(LoginBottomSheetContentType.AliasOptions)
    }
    var showRemoveAliasDialog by rememberSaveable { mutableStateOf(false) }
    var actionWhenKeyboardDisappears by remember { mutableStateOf<ActionAfterHideKeyboard?>(null) }

    val keyboardState by keyboardAsState()
    LaunchedEffect(keyboardState, actionWhenKeyboardDisappears) {
        if (!keyboardState) {
            when (actionWhenKeyboardDisappears) {
                ActionAfterHideKeyboard.ShowBottomSheet -> {
                    scope.launch {
                        bottomSheetState.forceExpand()
                        actionWhenKeyboardDisappears = null // Clear flag
                    }
                }

                ActionAfterHideKeyboard.CreateAlias -> {
                    scope.launch {
                        onCreateAlias(
                            uiState.selectedVault!!.vault.shareId,
                            uiState.loginItem.title.some()
                        )
                        actionWhenKeyboardDisappears = null // Clear flag
                    }
                }

                ActionAfterHideKeyboard.GeneratePassword -> {
                    scope.launch {
                        onGeneratePasswordClick()
                        actionWhenKeyboardDisappears = null // Clear flag
                    }
                }

                null -> Unit
            }
        }
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    PassModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetContent = {
            when (currentBottomSheet) {
                LoginBottomSheetContentType.AliasOptions -> AliasOptionsBottomSheet(
                    modifier = modifier,
                    onEditAliasClick = {
                        scope.launch {
                            bottomSheetState.hide()
                            onCreateAlias(
                                uiState.selectedVault!!.vault.shareId,
                                uiState.loginItem.title.some()
                            )
                        }
                    },
                    onRemoveAliasClick = {
                        scope.launch {
                            showRemoveAliasDialog = true
                            bottomSheetState.hide()
                        }
                    }
                )

                LoginBottomSheetContentType.VaultSelection -> VaultSelectionBottomSheet(
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
                    actionColor = PassTheme.colors.loginInteractionNormMajor1,
                    iconColor = PassTheme.colors.loginInteractionNormMajor2,
                    iconBackgroundColor = PassTheme.colors.loginInteractionNormMinor1,
                    onCloseClick = onUpClick,
                    onActionClick = { uiState.selectedVault?.vault?.shareId?.let(onSubmit) }
                )
            }
        ) { padding ->
            LoginItemForm(
                modifier = Modifier.padding(padding),
                loginItem = uiState.loginItem,
                hasReachedTotpLimit = uiState.hasReachedTotpLimit,
                selectedShare = uiState.selectedVault,
                showCreateAliasButton = showCreateAliasButton,
                canUpdateUsername = uiState.canUpdateUsername,
                primaryEmail = uiState.primaryEmail,
                isUpdate = isUpdate,
                isEditAllowed = uiState.isLoadingState == IsLoadingState.NotLoading,
                showVaultSelector = showVaultSelector,
                onTitleChange = onTitleChange,
                onTitleRequiredError = uiState.validationErrors.contains(LoginItemValidationErrors.BlankTitle),
                isTotpError = uiState.validationErrors.contains(LoginItemValidationErrors.InvalidTotp),
                onUsernameChange = onUsernameChange,
                onPasswordChange = onPasswordChange,
                onWebsiteChange = onWebsiteChange,
                focusLastWebsite = uiState.focusLastWebsite,
                doesWebsiteIndexHaveError = { idx ->
                    uiState.validationErrors.any {
                        if (it is LoginItemValidationErrors.InvalidUrl) {
                            it.index == idx
                        } else {
                            false
                        }
                    }
                },
                onNoteChange = onNoteChange,
                onGeneratePasswordClick = {
                    if (!keyboardState) {
                        // If keyboard is hidden, call the action directly
                        onGeneratePasswordClick()
                    } else {
                        // If keyboard is present, do it in a deferred way
                        actionWhenKeyboardDisappears = ActionAfterHideKeyboard.GeneratePassword
                        keyboardController?.hide()
                    }
                },
                onCreateAliasClick = {
                    if (!keyboardState) {
                        // If keyboard is hidden, call the action directly
                        onCreateAlias(
                            uiState.selectedVault!!.vault.shareId,
                            uiState.loginItem.title.toOption()
                        )
                    } else {
                        // If keyboard is present, do it in a deferred way
                        actionWhenKeyboardDisappears = ActionAfterHideKeyboard.CreateAlias
                        keyboardController?.hide()
                    }
                },
                onAliasOptionsClick = {
                    scope.launch {
                        currentBottomSheet = LoginBottomSheetContentType.AliasOptions
                        bottomSheetState.show()
                    }
                },
                onVaultSelectorClick = {
                    scope.launch {
                        currentBottomSheet = LoginBottomSheetContentType.VaultSelection
                        bottomSheetState.show()
                    }
                },
                onTotpChange = onTotpChange,
                onPasteTotpClick = onPasteTotpClick,
                onScanTotpClick = onScanTotpClick,
                onLinkedAppDelete = onLinkedAppDelete,
                onUpgrade = onUpgrade
            )

            ConfirmRemoveAliasDialog(
                show = showRemoveAliasDialog,
                onDismiss = { showRemoveAliasDialog = false },
                onCancel = { showRemoveAliasDialog = false },
                onConfirm = {
                    showRemoveAliasDialog = false
                    onRemoveAliasClick()
                }
            )

            ItemSavedLaunchedEffect(
                isItemSaved = uiState.isItemSaved,
                selectedShareId = uiState.selectedVault?.vault?.shareId,
                onSuccess = onSuccess
            )
        }
    }
}

@Composable
private fun ItemSavedLaunchedEffect(
    isItemSaved: ItemSavedState,
    selectedShareId: ShareId?,
    onSuccess: (ShareId, ItemId, ItemUiModel) -> Unit
) {
    if (isItemSaved !is ItemSavedState.Success) return
    selectedShareId ?: return
    LaunchedEffect(Unit) {
        onSuccess(
            selectedShareId,
            isItemSaved.itemId,
            isItemSaved.item
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Suppress("SwallowedException")
suspend fun ModalBottomSheetState.forceExpand() {
    try {
        animateTo(ModalBottomSheetValue.Expanded)
    } catch (e: CancellationException) {
        currentCoroutineContext().ensureActive()
        forceExpand()
    }
}
