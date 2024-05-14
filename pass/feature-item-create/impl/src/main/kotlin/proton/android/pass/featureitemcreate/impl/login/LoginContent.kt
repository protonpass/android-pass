/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.featureitemcreate.impl.login

import androidx.activity.compose.BackHandler
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
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.some
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.keyboard.keyboardAsState
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.domain.ShareId
import proton.android.pass.featureitemcreate.impl.common.CreateUpdateTopBar
import proton.android.pass.featureitemcreate.impl.login.LoginItemValidationErrors.InvalidUrl

private enum class ActionAfterHideKeyboard {
    GeneratePassword,
    CreateAlias
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun LoginContent(
    modifier: Modifier = Modifier,
    uiState: BaseLoginUiState,
    passkeyState: Option<CreatePasskeyState>,
    loginItemFormState: LoginItemFormState,
    selectedShareId: ShareId?,
    topBarActionName: String,
    showCreateAliasButton: Boolean,
    isUpdate: Boolean,
    onEvent: (LoginContentEvent) -> Unit,
    onNavigate: (BaseLoginNavigation) -> Unit
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
                            loginItemFormState.title.some()
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
                onUpgrade = { onNavigate(BaseLoginNavigation.Upgrade) }
            )
        }
    ) { padding ->
        LoginItemForm(
            modifier = Modifier.padding(padding),
            loginItemFormState = loginItemFormState,
            passkeyState = passkeyState,
            canUseCustomFields = uiState.canUseCustomFields,
            totpUiState = uiState.totpUiState,
            customFieldValidationErrors = uiState.validationErrors
                .filterIsInstance<LoginItemValidationErrors.CustomFieldValidationError>()
                .toPersistentList(),
            focusedField = uiState.focusedField,
            showCreateAliasButton = showCreateAliasButton,
            canUpdateUsername = uiState.canUpdateUsername,
            primaryEmail = uiState.primaryEmail,
            isUpdate = isUpdate,
            isEditAllowed = uiState.isLoadingState == IsLoadingState.NotLoading,
            isTotpError = uiState.validationErrors.contains(LoginItemValidationErrors.InvalidTotp),
            isTitleError = uiState.validationErrors.contains(LoginItemValidationErrors.BlankTitle),
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
                            loginItemFormState.title.some()
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
                        showUpgrade = uiState.hasReachedAliasLimit
                    )
                )
            },
            onNavigate = onNavigate
        )
    }
}
