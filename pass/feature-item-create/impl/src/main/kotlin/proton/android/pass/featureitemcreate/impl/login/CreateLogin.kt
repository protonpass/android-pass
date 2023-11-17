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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.commonui.api.OneTimeLaunchedEffect
import proton.android.pass.composecomponents.impl.dialogs.ConfirmCloseDialog
import proton.android.pass.composecomponents.impl.form.TitleVaultSelectionSection
import proton.android.pass.composecomponents.impl.keyboard.keyboardAsState
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.featureitemcreate.impl.R
import proton.android.pass.featureitemcreate.impl.common.ItemSavedLaunchedEffect
import proton.android.pass.featureitemcreate.impl.common.ShareError.EmptyShareList
import proton.android.pass.featureitemcreate.impl.common.ShareError.SharesNotAvailable
import proton.android.pass.featureitemcreate.impl.common.ShareUiState
import proton.android.pass.featureitemcreate.impl.launchedeffects.InAppReviewTriggerLaunchedEffect
import proton.android.pass.featureitemcreate.impl.login.customfields.CustomFieldEvent
import proton.android.pass.domain.ShareId

private enum class CLActionAfterHideKeyboard {
    SelectVault
}

@Suppress("ComplexMethod")
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CreateLoginScreen(
    modifier: Modifier = Modifier,
    initialContents: InitialCreateLoginUiState? = null,
    showCreateAliasButton: Boolean = true,
    clearAlias: Boolean,
    selectVault: ShareId?,
    onNavigate: (BaseLoginNavigation) -> Unit,
    viewModel: CreateLoginViewModel = hiltViewModel()
) {
    OneTimeLaunchedEffect(key = initialContents, saver = InitialCreateLoginUiStateSaver) {
        initialContents ?: return@OneTimeLaunchedEffect
        viewModel.setInitialContents(initialContents)
    }
    val uiState by viewModel.createLoginUiState.collectAsStateWithLifecycle()
    val keyboardState by keyboardAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    var actionWhenKeyboardDisappears by remember { mutableStateOf<CLActionAfterHideKeyboard?>(null) }

    LaunchedEffect(clearAlias) {
        if (clearAlias) {
            viewModel.onRemoveAlias()
        }
    }

    LaunchedEffect(selectVault) {
        if (selectVault != null) {
            viewModel.changeVault(selectVault)
        }
    }

    var showConfirmDialog by rememberSaveable { mutableStateOf(false) }
    val onExit = {
        if (uiState.baseLoginUiState.hasUserEditedContent) {
            showConfirmDialog = !showConfirmDialog
        } else {
            viewModel.onClose()
            onNavigate(BaseLoginNavigation.Close)
        }
    }
    BackHandler {
        onExit()
    }

    val (showVaultSelector, selectedVault) = when (val shares = uiState.shareUiState) {
        ShareUiState.Loading,
        ShareUiState.NotInitialised -> false to null

        is ShareUiState.Error -> {
            if (shares.shareError == EmptyShareList || shares.shareError == SharesNotAvailable) {
                viewModel.onEmitSnackbarMessage(LoginSnackbarMessages.InitError)
                LaunchedEffect(Unit) {
                    onNavigate(BaseLoginNavigation.Close)
                }
            }
            false to null
        }

        is ShareUiState.Success -> (shares.vaultList.size > 1) to shares.currentVault
    }
    Box(modifier = modifier.fillMaxSize()) {
        LoginContent(
            uiState = uiState.baseLoginUiState,
            loginItemFormState = viewModel.loginItemFormState,
            selectedShareId = selectedVault?.vault?.shareId,
            topBarActionName = stringResource(id = R.string.title_create_login),
            showCreateAliasButton = showCreateAliasButton,
            isUpdate = false,
            onEvent = {
                when (it) {
                    LoginContentEvent.Up -> onExit()
                    is LoginContentEvent.Submit -> viewModel.createItem()
                    is LoginContentEvent.OnUsernameChange -> viewModel.onUsernameChange(it.username)
                    is LoginContentEvent.OnPasswordChange -> viewModel.onPasswordChange(it.password)
                    is LoginContentEvent.OnWebsiteEvent -> when (val event = it.event) {
                        WebsiteSectionEvent.AddWebsite -> viewModel.onAddWebsite()
                        is WebsiteSectionEvent.RemoveWebsite -> viewModel.onRemoveWebsite(event.index)
                        is WebsiteSectionEvent.WebsiteValueChanged ->
                            viewModel.onWebsiteChange(event.value, event.index)
                    }

                    is LoginContentEvent.OnNoteChange -> viewModel.onNoteChange(it.note)
                    is LoginContentEvent.OnLinkedAppDelete -> {}
                    is LoginContentEvent.OnTotpChange -> viewModel.onTotpChange(it.totp)
                    LoginContentEvent.PasteTotp -> viewModel.onPasteTotp()
                    is LoginContentEvent.OnFocusChange ->
                        viewModel.onFocusChange(it.field, it.isFocused)

                    is LoginContentEvent.OnCustomFieldEvent -> {
                        when (val event = it.event) {
                            CustomFieldEvent.AddCustomField -> {
                                onNavigate(BaseLoginNavigation.AddCustomField)
                            }

                            is CustomFieldEvent.OnCustomFieldOptions -> {
                                onNavigate(
                                    BaseLoginNavigation.CustomFieldOptions(
                                        currentValue = event.currentLabel,
                                        index = event.index
                                    )
                                )
                            }

                            is CustomFieldEvent.OnValueChange -> {
                                viewModel.onCustomFieldChange(event.index, event.value)
                            }

                            CustomFieldEvent.Upgrade -> {
                                onNavigate(BaseLoginNavigation.Upgrade)
                            }

                            is CustomFieldEvent.FocusRequested ->
                                viewModel.onFocusChange(event.loginCustomField, event.isFocused)
                        }
                    }
                }
            },
            onNavigate = onNavigate,
            titleSection = {
                TitleVaultSelectionSection(
                    titleValue = viewModel.loginItemFormState.title,
                    showVaultSelector = showVaultSelector,
                    onTitleChanged = viewModel::onTitleChange,
                    onTitleRequiredError = uiState.baseLoginUiState.validationErrors.contains(
                        LoginItemValidationErrors.BlankTitle
                    ),
                    enabled = uiState.baseLoginUiState.isLoadingState == IsLoadingState.NotLoading,
                    vaultName = selectedVault?.vault?.name,
                    vaultColor = selectedVault?.vault?.color,
                    vaultIcon = selectedVault?.vault?.icon,
                    onVaultClicked = {
                        actionWhenKeyboardDisappears = CLActionAfterHideKeyboard.SelectVault
                        keyboardController?.hide()
                    }
                )
            }
        )

        ConfirmCloseDialog(
            show = showConfirmDialog,
            onCancel = {
                showConfirmDialog = false
            },
            onConfirm = {
                showConfirmDialog = false
                viewModel.onClose()
                onNavigate(BaseLoginNavigation.Close)
            }
        )
    }
    LaunchedEffect(keyboardState, actionWhenKeyboardDisappears) {
        if (!keyboardState) {
            when (actionWhenKeyboardDisappears) {
                CLActionAfterHideKeyboard.SelectVault -> {
                    selectedVault ?: return@LaunchedEffect
                    onNavigate(
                        BaseLoginNavigation.OnCreateLoginEvent(
                            CreateLoginNavigation.SelectVault(
                                selectedVault.vault.shareId
                            )
                        )
                    )
                    actionWhenKeyboardDisappears = null // Clear flag
                }

                null -> {}
            }
        }
    }

    ItemSavedLaunchedEffect(
        isItemSaved = uiState.baseLoginUiState.isItemSaved,
        selectedShareId = selectedVault?.vault?.shareId,
        onSuccess = { _, _, model ->
            val event = CreateLoginNavigation.LoginCreated(model)
            onNavigate(BaseLoginNavigation.OnCreateLoginEvent(event))
        }
    )
    InAppReviewTriggerLaunchedEffect(
        triggerCondition = uiState.baseLoginUiState.isItemSaved is ItemSavedState.Success,
    )
}
