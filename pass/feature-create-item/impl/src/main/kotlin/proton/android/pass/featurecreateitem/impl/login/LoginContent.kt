package proton.android.pass.featurecreateitem.impl.login

import androidx.annotation.StringRes
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
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import me.proton.core.compose.component.ProtonModalBottomSheetLayout
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.featurecreateitem.impl.ItemSavedState
import proton.android.pass.featurecreateitem.impl.login.LoginSnackbarMessages.EmptyShareIdError
import proton.android.pass.featurecreateitem.impl.login.bottomsheet.AddTotpBottomSheet
import proton.android.pass.featurecreateitem.impl.login.bottomsheet.AddTotpType
import proton.android.pass.featurecreateitem.impl.login.bottomsheet.AliasOptionsBottomSheet
import proton.android.pass.featurecreateitem.impl.login.bottomsheet.GeneratePasswordBottomSheet
import proton.android.pass.featurecreateitem.impl.login.bottomsheet.LoginBottomSheetContentType.AddTotp
import proton.android.pass.featurecreateitem.impl.login.bottomsheet.LoginBottomSheetContentType.AliasOptions
import proton.android.pass.featurecreateitem.impl.login.bottomsheet.LoginBottomSheetContentType.GeneratePassword
import proton.android.pass.featurecreateitem.impl.login.bottomsheet.LoginBottomSheetContentType.VaultSelection
import proton.android.pass.featurecreateitem.impl.login.bottomsheet.VaultSelectionBottomSheet
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun LoginContent(
    modifier: Modifier = Modifier,
    @StringRes topBarTitle: Int,
    @StringRes topBarActionName: Int,
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
    onEmitSnackbarMessage: (LoginSnackbarMessages) -> Unit,
    onCreateAliasClick: (ShareId, Option<String>) -> Unit,
    onRemoveAliasClick: () -> Unit,
    onDeleteItemClick: () -> Unit,
    onVaultSelect: (ShareId) -> Unit,
    onAddTotp: (AddTotpType) -> Unit
) {
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )

    var regeneratePassword by remember { mutableStateOf(true) }
    var currentBottomSheet by remember { mutableStateOf(GeneratePassword) }
    var showRemoveAliasDialog by remember { mutableStateOf(false) }

    ProtonModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetContent = {
            when (currentBottomSheet) {
                GeneratePassword -> GeneratePasswordBottomSheet(
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
                AliasOptions -> AliasOptionsBottomSheet(
                    modifier = modifier,
                    onRemoveAliasClick = {
                        scope.launch {
                            showRemoveAliasDialog = true
                            bottomSheetState.hide()
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
                AddTotp -> AddTotpBottomSheet(
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
            topBar = {
                LoginTopBar(
                    topBarTitle = topBarTitle,
                    topBarActionName = topBarActionName,
                    shareUiModel = uiState.selectedShareId,
                    isLoadingState = uiState.isLoadingState,
                    onUpClick = onUpClick,
                    onSubmit = onSubmit
                )
            }
        ) { padding ->
            LoginItemForm(
                modifier = modifier.padding(padding),
                loginItem = uiState.loginItem,
                selectedShare = uiState.selectedShareId,
                showCreateAliasButton = showCreateAliasButton,
                canUpdateUsername = uiState.canUpdateUsername,
                isUpdate = isUpdate,
                onTitleChange = onTitleChange,
                onTitleRequiredError = uiState.validationErrors.contains(LoginItemValidationErrors.BlankTitle),
                onUsernameChange = onUsernameChange,
                onPasswordChange = onPasswordChange,
                onWebsiteChange = onWebsiteChange,
                focusLastWebsite = uiState.focusLastWebsite,
                isEditAllowed = uiState.isLoadingState == IsLoadingState.NotLoading,
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
                        currentBottomSheet = GeneratePassword
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
                        currentBottomSheet = AliasOptions
                        bottomSheetState.show()
                    }
                },
                onDeleteClick = onDeleteItemClick,
                onVaultSelectorClick = {
                    scope.launch {
                        currentBottomSheet = VaultSelection
                        bottomSheetState.show()
                    }
                },
                onAddTotpClick = {
                    scope.launch {
                        currentBottomSheet = AddTotp
                        bottomSheetState.show()
                    }
                }
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

            LaunchedEffect(uiState.isItemSaved is ItemSavedState.Success) {
                val isItemSaved = uiState.isItemSaved
                if (isItemSaved is ItemSavedState.Success) {
                    if (uiState.selectedShareId != null) {
                        onSuccess(
                            uiState.selectedShareId.id,
                            isItemSaved.itemId,
                            isItemSaved.item
                        )
                    } else {
                        onEmitSnackbarMessage(EmptyShareIdError)
                    }
                }
            }
        }
    }
}
