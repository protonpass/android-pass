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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.common.api.Option
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault
import proton.android.pass.featureitemcreate.impl.common.CreateUpdateTopBar
import proton.android.pass.featureitemcreate.impl.login.LoginItemValidationErrors.InvalidUrl

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun LoginContent(
    modifier: Modifier = Modifier,
    uiState: BaseLoginUiState,
    passkeyState: Option<CreatePasskeyState>,
    loginItemFormState: LoginItemFormState,
    selectedVault: Vault?,
    showVaultSelector: Boolean,
    selectedShareId: ShareId?,
    topBarActionName: String,
    showCreateAliasButton: Boolean,
    isUpdate: Boolean,
    onEvent: (LoginContentEvent) -> Unit
) {
    BackHandler { onEvent(LoginContentEvent.Up) }
    Scaffold(
        modifier = modifier,
        topBar = {
            CreateUpdateTopBar(
                text = topBarActionName,
                isLoading = uiState.isLoadingState.value(),
                actionColor = PassTheme.colors.loginInteractionNormMajor1,
                iconColor = PassTheme.colors.loginInteractionNormMajor2,
                iconBackgroundColor = PassTheme.colors.loginInteractionNormMinor1,
                selectedVault = selectedVault,
                showVaultSelector = showVaultSelector,
                onCloseClick = { onEvent(LoginContentEvent.Up) },
                onActionClick = {
                    selectedShareId ?: return@CreateUpdateTopBar
                    onEvent(LoginContentEvent.Submit(selectedShareId))
                },
                onUpgrade = { onEvent(LoginContentEvent.OnUpgrade) },
                onVaultSelectorClick = {
                    selectedShareId ?: return@CreateUpdateTopBar
                    onEvent(LoginContentEvent.OnVaultSelect(selectedShareId))
                }
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
            focusLastWebsite = uiState.focusLastWebsite,
            websitesWithErrors = uiState.validationErrors
                .filterIsInstance<InvalidUrl>()
                .map { it.index }
                .toPersistentList(),
            selectedShareId = selectedShareId,
            hasReachedAliasLimit = uiState.hasReachedAliasLimit,
            isUsernameSplitTooltipEnabled = uiState.isUsernameSplitTooltipEnabled,
            isFileAttachmentsEnabled = uiState.isFileAttachmentsEnabled,
            attachmentsState = uiState.attachmentsState,
            onEvent = onEvent
        )
    }
}
