package me.proton.pass.presentation.create.login

import androidx.annotation.StringRes
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
import me.proton.android.pass.ui.shared.LoadingDialog
import me.proton.core.compose.component.ProtonModalBottomSheetLayout
import me.proton.pass.common.api.None
import me.proton.pass.common.api.Some
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ShareId
import me.proton.pass.presentation.create.login.LoginSnackbarMessages.EmptyShareIdError
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
    onSuccess: (ShareId, ItemId) -> Unit,
    onSubmit: (ShareId) -> Unit,
    onTitleChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onWebsiteChange: OnWebsiteChange,
    onNoteChange: (String) -> Unit,
    onEmitSnackbarMessage: (LoginSnackbarMessages) -> Unit,
    onCreateAliasClick: (ShareId) -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    val scope = rememberCoroutineScope()

    val (regeneratePassword, setRegeneratePassword) = remember { mutableStateOf(true) }

    ProtonModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetContent = {
            GeneratePasswordBottomSheet(
                regeneratePassword = regeneratePassword,
                onPasswordRegenerated = {
                    setRegeneratePassword(false)
                },
                onConfirm = { password ->
                    scope.launch {
                        onPasswordChange(password)
                        bottomSheetState.hide()
                    }
                }
            )
        }
    ) {
        Scaffold(
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
                loginItem = uiState.loginItem,
                modifier = modifier.padding(padding),
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
                        setRegeneratePassword(true)
                        bottomSheetState.show()
                    }
                },
                onCreateAliasClick = {
                    if (uiState.shareId is Some) {
                        onCreateAliasClick(uiState.shareId.value)
                    }
                }
            )
            LaunchedEffect(uiState.isItemSaved is ItemSavedState.Success) {
                val isItemSaved = uiState.isItemSaved
                if (isItemSaved is ItemSavedState.Success) {
                    when (uiState.shareId) {
                        None -> onEmitSnackbarMessage(EmptyShareIdError)
                        is Some -> onSuccess(uiState.shareId.value, isItemSaved.itemId)
                    }
                }
            }
        }
    }
}
