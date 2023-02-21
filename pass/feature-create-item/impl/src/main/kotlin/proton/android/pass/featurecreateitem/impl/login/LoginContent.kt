package proton.android.pass.featurecreateitem.impl.login

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
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.PassColors
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.commonuimodels.api.ShareUiModel
import proton.android.pass.composecomponents.impl.bottomsheet.PassModalBottomSheetLayout
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.featurecreateitem.impl.ItemSavedState
import proton.android.pass.featurecreateitem.impl.common.CreateUpdateTopBar
import proton.android.pass.featurecreateitem.impl.login.bottomsheet.AddTotpBottomSheet
import proton.android.pass.featurecreateitem.impl.login.bottomsheet.AddTotpType
import proton.android.pass.featurecreateitem.impl.login.bottomsheet.AliasOptionsBottomSheet
import proton.android.pass.featurecreateitem.impl.login.bottomsheet.GeneratePasswordBottomSheet
import proton.android.pass.featurecreateitem.impl.login.bottomsheet.LoginBottomSheetContentType
import proton.android.pass.featurecreateitem.impl.login.bottomsheet.VaultSelectionBottomSheet
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
    onUpClick: () -> Unit,
    onSuccess: (ShareId, ItemId, ItemUiModel) -> Unit,
    onSubmit: (ShareId) -> Unit,
    onTitleChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onWebsiteChange: OnWebsiteChange,
    onNoteChange: (String) -> Unit,
    onCreateAliasClick: (ShareId, Option<String>) -> Unit,
    onRemoveAliasClick: () -> Unit,
    onVaultSelect: (ShareId) -> Unit,
    onAddTotp: (AddTotpType) -> Unit,
    onDeleteTotp: () -> Unit,
    onLinkedAppDelete: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )

    var regeneratePassword by remember { mutableStateOf(true) }
    var currentBottomSheet by remember { mutableStateOf(LoginBottomSheetContentType.GeneratePassword) }
    var showRemoveAliasDialog by remember { mutableStateOf(false) }

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
                    }
                )
                LoginBottomSheetContentType.AliasOptions -> AliasOptionsBottomSheet(
                    modifier = modifier,
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
                LoginBottomSheetContentType.AddTotp -> AddTotpBottomSheet(
                    onAddTotp = {
                        onAddTotp(it)
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
                    color = PassColors.PurpleAccent,
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
                        bottomSheetState.show()
                    }
                },
                onCreateAliasClick = {
                    if (uiState.selectedShareId != null) {
                        onCreateAliasClick(
                            uiState.selectedShareId.id,
                            uiState.loginItem.title.toOption()
                        )
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
                onAddTotpClick = {
                    scope.launch {
                        currentBottomSheet = LoginBottomSheetContentType.AddTotp
                        bottomSheetState.show()
                    }
                },
                onDeleteTotpClick = onDeleteTotp,
                onLinkedAppDelete = onLinkedAppDelete
            )

            if (showRemoveAliasDialog) {
                ConfirmRemoveAliasDialog(
                    onDismiss = { showRemoveAliasDialog = false },
                    onCancel = { showRemoveAliasDialog = false },
                    onConfirm = {
                        showRemoveAliasDialog = false
                        onRemoveAliasClick()
                    }
                )
            }

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
