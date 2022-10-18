package me.proton.core.pass.presentation.create.login

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.proton.android.pass.ui.shared.CrossBackIcon
import me.proton.android.pass.ui.shared.LoadingDialog
import me.proton.android.pass.ui.shared.TopBarTitleView
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.pass.common.api.None
import me.proton.core.pass.common.api.Some
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.presentation.uievents.IsLoadingState
import me.proton.core.pass.presentation.uievents.ItemSavedState

@ExperimentalComposeUiApi
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
    onSnackbarMessage: (LoginSnackbarMessages) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Scaffold(
        topBar = {
            ProtonTopAppBar(
                title = { TopBarTitleView(topBarTitle) },
                navigationIcon = { CrossBackIcon(onUpClick = onUpClick) },
                actions = {
                    IconButton(
                        onClick = {
                            keyboardController?.hide()
                            when (uiState.shareId) {
                                None -> onSnackbarMessage(LoginSnackbarMessages.EmptyShareIdError)
                                is Some -> onSubmit(uiState.shareId.value)
                            }
                        },
                        modifier = Modifier.padding(end = 10.dp)
                    ) {
                        Text(
                            text = stringResource(topBarActionName),
                            color = ProtonTheme.colors.brandNorm,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.W500
                        )
                    }
                }
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
            onNoteChange = onNoteChange
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
