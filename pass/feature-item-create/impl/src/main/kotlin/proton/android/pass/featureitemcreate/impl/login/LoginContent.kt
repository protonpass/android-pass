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
    onEvent: (LoginContentEvent) -> Unit,
    onNavigate: (BaseLoginNavigation) -> Unit,
    titleSection: @Composable ColumnScope.() -> Unit
) {
    BackHandler {
        onEvent(LoginContentEvent.Up)
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
                            uiState.contents.title.some()
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
                onCloseClick = { onEvent(LoginContentEvent.Up) },
                onActionClick = {
                    selectedShareId ?: return@CreateUpdateTopBar
                    onEvent(LoginContentEvent.Submit(selectedShareId))
                },
                onUpgrade = {}
            )
        }
    ) { padding ->
        LoginItemForm(
            modifier = Modifier.padding(padding),
            contents = uiState.contents,
            totpUiState = uiState.totpUiState,
            customFieldsState = uiState.customFieldsState,
            showCreateAliasButton = showCreateAliasButton,
            canUpdateUsername = uiState.canUpdateUsername,
            primaryEmail = uiState.primaryEmail,
            isUpdate = isUpdate,
            isEditAllowed = uiState.isLoadingState == IsLoadingState.NotLoading,
            isTotpError = uiState.validationErrors.contains(LoginItemValidationErrors.InvalidTotp),
            onEvent = onEvent,
            focusLastWebsite = uiState.focusLastWebsite,
            websitesWithErrors = uiState.validationErrors
                .filterIsInstance<InvalidUrl>()
                .map { it.index }
                .toPersistentList(),
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
                            uiState.contents.title.some()
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
            onNavigate = onNavigate,
            titleSection = titleSection
        )

        ItemSavedLaunchedEffect(
            isItemSaved = uiState.isItemSaved,
            selectedShareId = selectedShareId,
            onSuccess = { shareId, itemId, model ->
                onEvent(LoginContentEvent.Success(shareId, itemId, model))
            }
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
