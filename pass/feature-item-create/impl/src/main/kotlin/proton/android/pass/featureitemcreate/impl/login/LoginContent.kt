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
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.commonuimodels.api.PackageInfoUi
import proton.android.pass.commonuimodels.api.ShareUiModel
import proton.android.pass.composecomponents.impl.bottomsheet.PassModalBottomSheetLayout
import proton.android.pass.composecomponents.impl.keyboard.keyboardAsState
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.featureitemcreate.impl.alias.AliasItem
import proton.android.pass.featureitemcreate.impl.alias.bottomsheet.CreateAliasBottomSheet
import proton.android.pass.featureitemcreate.impl.alias.saver.LoginBottomSheetContentTypeSaver
import proton.android.pass.featureitemcreate.impl.common.CreateUpdateTopBar
import proton.android.pass.featureitemcreate.impl.login.bottomsheet.AliasOptionsBottomSheet
import proton.android.pass.featureitemcreate.impl.login.bottomsheet.LoginBottomSheetContentType
import proton.android.pass.featureitemcreate.impl.login.bottomsheet.VaultSelectionBottomSheet
import proton.android.pass.featureitemcreate.impl.login.bottomsheet.password.GeneratePasswordBottomSheet
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

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
    onAliasCreated: (AliasItem) -> Unit
) {
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )

    BackHandler(enabled = bottomSheetState.isVisible) {
        scope.launch {
            bottomSheetState.hide()
        }
    }

    var regeneratePassword by remember { mutableStateOf(true) }
    var currentBottomSheet by rememberSaveable(stateSaver = LoginBottomSheetContentTypeSaver) {
        mutableStateOf(LoginBottomSheetContentType.GeneratePassword)
    }
    var showRemoveAliasDialog by rememberSaveable { mutableStateOf(false) }
    var showBottomSheetWhenKeyboardDisappears by rememberSaveable { mutableStateOf(false) }

    val keyboardState by keyboardAsState()
    LaunchedEffect(keyboardState, showBottomSheetWhenKeyboardDisappears) {
        if (!keyboardState && showBottomSheetWhenKeyboardDisappears) {
            scope.launch {
                bottomSheetState.forceExpand()
                showBottomSheetWhenKeyboardDisappears = false // Clear flag
            }
        }
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    PassModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetContent = {
            when (currentBottomSheet) {
                LoginBottomSheetContentType.GeneratePassword -> GeneratePasswordBottomSheet(
                    modifier = modifier,
                    regeneratePassword = regeneratePassword,
                    onPasswordRegenerated = {
                        regeneratePassword = false
                    },
                    onConfirm = { password ->
                        onPasswordChange(password)
                        scope.launch { bottomSheetState.hide() }
                    },
                    onDismiss = { scope.launch { bottomSheetState.hide() } }
                )
                LoginBottomSheetContentType.AliasOptions -> AliasOptionsBottomSheet(
                    modifier = modifier,
                    onEditAliasClick = {
                        scope.launch {
                            bottomSheetState.hide()
                            currentBottomSheet = LoginBottomSheetContentType.CreateAlias
                            bottomSheetState.show()
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
                    shareList = uiState.shareList,
                    selectedShare = uiState.selectedShareId!!,
                    onVaultClick = {
                        onVaultSelect(it)
                        scope.launch {
                            bottomSheetState.hide()
                        }
                    }
                )
                LoginBottomSheetContentType.CreateAlias -> CreateAliasBottomSheet(
                    itemTitle = uiState.loginItem.title,
                    aliasItem = uiState.aliasItem,
                    onAliasCreated = {
                        scope.launch {
                            onAliasCreated(it)
                            bottomSheetState.hide()
                        }
                    },
                    onCancel = {
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
                    opaqueColor = PassTheme.colors.accentPurpleOpaque,
                    weakestColor = PassTheme.colors.accentPurpleWeakest,
                    onCloseClick = onUpClick,
                    onActionClick = { uiState.selectedShareId?.id?.let(onSubmit) }
                )
            }
        ) { padding ->
            LoginItemForm(
                modifier = Modifier.padding(padding),
                loginItem = uiState.loginItem,
                selectedShare = uiState.selectedShareId,
                showCreateAliasButton = showCreateAliasButton,
                canUpdateUsername = uiState.canUpdateUsername,
                primaryEmail = uiState.primaryEmail,
                isUpdate = isUpdate,
                isEditAllowed = uiState.isLoadingState == IsLoadingState.NotLoading,
                showVaultSelector = showVaultSelector,
                onTitleChange = onTitleChange,
                onTitleRequiredError = uiState.validationErrors.contains(LoginItemValidationErrors.BlankTitle),
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
                    scope.launch {
                        currentBottomSheet = LoginBottomSheetContentType.GeneratePassword
                        regeneratePassword = true
                        if (!keyboardState) {
                            // If keyboard is hidden, display the bottomsheet
                            bottomSheetState.show()
                        } else {
                            // If keyboard is present, do it in a deferred way
                            showBottomSheetWhenKeyboardDisappears = true
                            keyboardController?.hide()
                        }
                    }
                },
                onCreateAliasClick = {
                    scope.launch {
                        currentBottomSheet = LoginBottomSheetContentType.CreateAlias
                        if (!keyboardState) {
                            // If keyboard is hidden, display the bottomsheet
                            bottomSheetState.show()
                        } else {
                            // If keyboard is present, do it in a deferred way
                            showBottomSheetWhenKeyboardDisappears = true
                            keyboardController?.hide()
                        }
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
                onLinkedAppDelete = onLinkedAppDelete
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
                selectedShareId = uiState.selectedShareId,
                onSuccess = onSuccess
            )
        }
    }
}

@Composable
private fun ItemSavedLaunchedEffect(
    isItemSaved: ItemSavedState,
    selectedShareId: ShareUiModel?,
    onSuccess: (ShareId, ItemId, ItemUiModel) -> Unit
) {
    if (isItemSaved !is ItemSavedState.Success) return
    selectedShareId ?: return
    LaunchedEffect(Unit) {
        onSuccess(
            selectedShareId.id,
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
