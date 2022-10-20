package me.proton.core.pass.presentation.create.login

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import me.proton.android.pass.ui.shared.LoadingDialog
import me.proton.core.compose.component.ProtonModalBottomSheetLayout
import me.proton.core.pass.common.api.None
import me.proton.core.pass.common.api.Some
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.presentation.uievents.IsLoadingState
import me.proton.core.pass.presentation.uievents.ItemSavedState

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
    snackbarHost: @Composable (SnackbarHostState) -> Unit,
    onSnackbarMessage: (LoginSnackbarMessages) -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    val scope = rememberCoroutineScope()

    ProtonModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetContent = {
            GeneratePasswordBottomSheet(
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
                    onSnackbarMessage = onSnackbarMessage
                )
            },
            snackbarHost = snackbarHost
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
                onNoteChange = onNoteChange,
                onGeneratePasswordClick = {
                    scope.launch { bottomSheetState.show() }
                }
            )
            LaunchedEffect(uiState.isItemSaved is ItemSavedState.Success) {
                val isItemSaved = uiState.isItemSaved
                if (isItemSaved is ItemSavedState.Success) {
                    when (uiState.shareId) {
                        None -> onSnackbarMessage(LoginSnackbarMessages.EmptyShareIdError)
                        is Some -> onSuccess(uiState.shareId.value, isItemSaved.itemId)
                    }
                }
            }
        }
    }
}
