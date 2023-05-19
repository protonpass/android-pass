package proton.android.pass.featureitemcreate.impl.login

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.common.api.some
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.commonuimodels.api.PackageInfoUi
import proton.android.pass.composecomponents.impl.keyboard.keyboardAsState
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.featureitemcreate.impl.common.CreateUpdateTopBar
import proton.android.pass.featureitemcreate.impl.login.LoginItemValidationErrors.InvalidUrl
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

private enum class ActionAfterHideKeyboard {
    GeneratePassword,
    CreateAlias
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun LoginContent(
    modifier: Modifier = Modifier,
    uiState: BaseLoginUiState,
    selectedShareId: ShareId?,
    topBarActionName: String,
    showCreateAliasButton: Boolean,
    isUpdate: Boolean,
    onUpClick: () -> Unit,
    onSuccess: (ShareId, ItemId, ItemUiModel) -> Unit,
    onSubmit: (ShareId) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onWebsiteSectionEvent: (WebsiteSectionEvent) -> Unit,
    onNoteChange: (String) -> Unit,
    onTotpChange: (String) -> Unit,
    onPasteTotpClick: () -> Unit,
    onLinkedAppDelete: (PackageInfoUi) -> Unit,
    onNavigate: (BaseLoginNavigation) -> Unit,
    titleSection: @Composable ColumnScope.() -> Unit
) {
    BackHandler {
        onUpClick()
    }

    var actionWhenKeyboardDisappears by remember { mutableStateOf<ActionAfterHideKeyboard?>(null) }

    val keyboardState by keyboardAsState()

    LaunchedEffect(keyboardState, actionWhenKeyboardDisappears) {
        if (!keyboardState) {
            when (actionWhenKeyboardDisappears) {
                ActionAfterHideKeyboard.CreateAlias -> {
                    selectedShareId ?: return@LaunchedEffect
                    onNavigate(
                        BaseLoginNavigation.CreateAlias(
                            selectedShareId,
                            uiState.hasReachedAliasLimit,
                            uiState.loginItem.title.some()
                        )
                    )
                    actionWhenKeyboardDisappears = null // Clear flag
                }

                ActionAfterHideKeyboard.GeneratePassword -> {
                    onNavigate(BaseLoginNavigation.GeneratePassword)
                    actionWhenKeyboardDisappears = null // Clear flag
                }

                null -> Unit
            }
        }
    }

    val keyboardController = LocalSoftwareKeyboardController.current
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
                onActionClick = {
                    selectedShareId ?: return@CreateUpdateTopBar
                    onSubmit(selectedShareId)
                },
                onUpgrade = {}
            )
        }
    ) { padding ->
        LoginItemForm(
            modifier = Modifier.padding(padding),
            loginItem = uiState.loginItem,
            totpUiState = uiState.totpUiState,
            selectedShareId = selectedShareId,
            showCreateAliasButton = showCreateAliasButton,
            canUpdateUsername = uiState.canUpdateUsername,
            primaryEmail = uiState.primaryEmail,
            isUpdate = isUpdate,
            isEditAllowed = uiState.isLoadingState == IsLoadingState.NotLoading,
            isTotpError = uiState.validationErrors.contains(LoginItemValidationErrors.InvalidTotp),
            onUsernameChange = onUsernameChange,
            onPasswordChange = onPasswordChange,
            onWebsiteSectionEvent = onWebsiteSectionEvent,
            focusLastWebsite = uiState.focusLastWebsite,
            websitesWithErrors = uiState.validationErrors
                .filterIsInstance<InvalidUrl>()
                .map { it.index }
                .toPersistentList(),
            onNoteChange = onNoteChange,
            onGeneratePasswordClick = {
                if (!keyboardState) {
                    // If keyboard is hidden, call the action directly
                    onNavigate(BaseLoginNavigation.GeneratePassword)
                } else {
                    // If keyboard is present, do it in a deferred way
                    actionWhenKeyboardDisappears = ActionAfterHideKeyboard.GeneratePassword
                    keyboardController?.hide()
                }
            },
            onCreateAliasClick = {
                if (!keyboardState) {
                    // If keyboard is hidden, call the action directly
                    selectedShareId ?: return@LoginItemForm
                    onNavigate(
                        BaseLoginNavigation.CreateAlias(
                            selectedShareId,
                            uiState.hasReachedAliasLimit,
                            uiState.loginItem.title.some()
                        )
                    )
                } else {
                    // If keyboard is present, do it in a deferred way
                    actionWhenKeyboardDisappears = ActionAfterHideKeyboard.CreateAlias
                    keyboardController?.hide()
                }
            },
            onAliasOptionsClick = {
                selectedShareId ?: return@LoginItemForm
                onNavigate(
                    BaseLoginNavigation.AliasOptions(
                        shareId = selectedShareId,
                        showUpgrade = uiState.hasReachedAliasLimit,
                    )
                )
            },
            onTotpChange = onTotpChange,
            onPasteTotpClick = onPasteTotpClick,
            onLinkedAppDelete = onLinkedAppDelete,
            onNavigate = onNavigate,
            titleSection = titleSection
        )

        ItemSavedLaunchedEffect(
            isItemSaved = uiState.isItemSaved,
            selectedShareId = selectedShareId,
            onSuccess = onSuccess
        )
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
