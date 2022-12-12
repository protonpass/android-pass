package me.proton.pass.presentation.create.login

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
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
import me.proton.pass.common.api.None
import me.proton.pass.common.api.Some
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ShareId
import me.proton.pass.presentation.components.common.LoadingDialog
import me.proton.pass.presentation.components.common.bottomsheet.PassModalBottomSheetLayout
import me.proton.pass.presentation.components.model.ItemUiModel
import me.proton.pass.presentation.create.login.LoginSnackbarMessages.EmptyShareIdError
import me.proton.pass.presentation.create.login.bottomsheet.LoginBottomSheet
import me.proton.pass.presentation.create.login.bottomsheet.LoginBottomSheetContent
import me.proton.pass.presentation.uievents.IsLoadingState
import me.proton.pass.presentation.uievents.ItemSavedState

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class)
@Composable
internal fun LoginContent(
    modifier: Modifier = Modifier,
    @StringRes topBarTitle: Int,
    @StringRes topBarActionName: Int,
    uiState: CreateUpdateLoginUiState,
    onUpClick: () -> Unit,
    onSuccess: (ShareId, ItemId, ItemUiModel) -> Unit,
    onSubmit: (ShareId) -> Unit,
    onTitleChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onWebsiteChange: OnWebsiteChange,
    onNoteChange: (String) -> Unit,
    onEmitSnackbarMessage: (LoginSnackbarMessages) -> Unit,
    onCreateAliasClick: (ShareId) -> Unit,
    onRemoveAliasClick: () -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    val scope = rememberCoroutineScope()

    val (regeneratePassword, setRegeneratePassword) = remember { mutableStateOf(true) }
    val (bottomSheetContent, setBottomSheetContent) = remember {
        mutableStateOf<LoginBottomSheetContent>(LoginBottomSheetContent.GeneratePassword)
    }
    val (showRemoveAliasDialog, setShowRemoveAliasDialog) = remember { mutableStateOf(false) }

    PassModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetContent = {
            LoginBottomSheet(
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding(),
                content = bottomSheetContent,
                regeneratePassword = regeneratePassword,
                setRegeneratePassword = setRegeneratePassword,
                onPasswordChange = { password -> onPasswordChange(password) },
                hideBottomSheet = { scope.launch { bottomSheetState.hide() } },
                onEditAliasClick = {},
                onRemoveAliasClick = {
                    scope.launch {
                        setShowRemoveAliasDialog(true)
                        bottomSheetState.hide()
                    }
                }
            )
        }
    ) {
        Scaffold(
            modifier = modifier,
            topBar = {
                LoginTopBar(
                    uiState = uiState,
                    topBarTitle = topBarTitle,
                    topBarActionName = topBarActionName,
                    onUpClick = onUpClick,
                    onSubmit = onSubmit,
                    onSnackbarMessage = onEmitSnackbarMessage
                )
            }
        ) { padding ->
            if (uiState.isLoadingState == IsLoadingState.Loading) {
                LoadingDialog()
            }
            LoginItemForm(
                modifier = Modifier.padding(padding),
                loginItem = uiState.loginItem,
                canUpdateUsername = uiState.canUpdateUsername,
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
                        setBottomSheetContent(LoginBottomSheetContent.GeneratePassword)
                        setRegeneratePassword(true)
                        bottomSheetState.show()
                    }
                },
                onCreateAliasClick = {
                    if (uiState.shareId is Some) {
                        onCreateAliasClick(uiState.shareId.value)
                    }
                },
                onAliasOptionsClick = {
                    scope.launch {
                        setBottomSheetContent(LoginBottomSheetContent.AliasOptions)
                        bottomSheetState.show()
                    }
                }
            )

            if (showRemoveAliasDialog) {
                ConfirmRemoveAliasDialog(
                    onDismiss = { setShowRemoveAliasDialog(false) },
                    onCancel = { setShowRemoveAliasDialog(false) },
                    onConfirm = {
                        setShowRemoveAliasDialog(false)
                        onRemoveAliasClick()
                    }
                )
            }

            LaunchedEffect(uiState.isItemSaved is ItemSavedState.Success) {
                val isItemSaved = uiState.isItemSaved
                if (isItemSaved is ItemSavedState.Success) {
                    when (uiState.shareId) {
                        None -> onEmitSnackbarMessage(EmptyShareIdError)
                        is Some -> onSuccess(
                            uiState.shareId.value,
                            isItemSaved.itemId,
                            isItemSaved.item
                        )
                    }
                }
            }
        }
    }
}
