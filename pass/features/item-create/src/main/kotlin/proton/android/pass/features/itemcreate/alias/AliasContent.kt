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

package proton.android.pass.features.itemcreate.alias

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault
import proton.android.pass.features.itemcreate.common.AliasItemValidationError
import proton.android.pass.features.itemcreate.common.CommonFieldValidationError
import proton.android.pass.features.itemcreate.common.CreateUpdateTopBar
import proton.android.pass.features.itemcreate.common.CustomFieldValidationError

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun AliasContent(
    modifier: Modifier = Modifier,
    uiState: BaseAliasUiState,
    aliasItemFormState: AliasItemFormState,
    selectedVault: Vault?,
    selectedShareId: ShareId?,
    showVaultSelector: Boolean,
    topBarActionName: String,
    isCreateMode: Boolean,
    isEditAllowed: Boolean,
    isAliasCreatedByUser: Boolean,
    canUseAttachments: Boolean,
    onEvent: (AliasContentUiEvent) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CreateUpdateTopBar(
                text = topBarActionName,
                isLoading = uiState.isLoadingState.value() ||
                    uiState.attachmentsState.loadingDraftAttachments.isNotEmpty(),
                showVaultSelector = showVaultSelector,
                actionColor = PassTheme.colors.aliasInteractionNormMajor1,
                iconColor = PassTheme.colors.aliasInteractionNormMajor2,
                showUpgrade = uiState.hasReachedAliasLimit,
                iconBackgroundColor = PassTheme.colors.aliasInteractionNormMinor1,
                selectedVault = selectedVault,
                onCloseClick = { onEvent(AliasContentUiEvent.Back) },
                onActionClick = {
                    selectedShareId ?: return@CreateUpdateTopBar
                    onEvent(AliasContentUiEvent.Submit(selectedShareId))
                },
                onUpgrade = { onEvent(AliasContentUiEvent.OnUpgrade) },
                onVaultSelectorClick = {
                    selectedShareId ?: return@CreateUpdateTopBar
                    onEvent(AliasContentUiEvent.OnVaultSelect(selectedShareId))
                }
            )
        }
    ) { padding ->
        AliasItemForm(
            modifier = Modifier.padding(padding),
            aliasItemFormState = aliasItemFormState,
            isCreateMode = isCreateMode,
            isAliasCreatedByUser = isAliasCreatedByUser,
            isEditAllowed = isEditAllowed,
            isLoading = uiState.isLoadingState.value(),
            showUpgrade = uiState.hasReachedAliasLimit,
            onTitleRequiredError = uiState.errorList.contains(CommonFieldValidationError.BlankTitle),
            onAliasRequiredError = uiState.errorList.contains(AliasItemValidationError.BlankPrefix),
            onInvalidAliasError = uiState.errorList.contains(AliasItemValidationError.InvalidAliasContent),
            onSuffixClick = { onEvent(AliasContentUiEvent.OnSuffixSelect) },
            onMailboxClick = { onEvent(AliasContentUiEvent.OnMailboxSelect) },
            onEvent = onEvent,
            isFileAttachmentsEnabled = uiState.isFileAttachmentEnabled && canUseAttachments,
            displayAdvancedOptionsBanner = uiState.displayAdvancedOptionsBanner && isCreateMode,
            displayFileAttachmentsOnboarding = uiState.displayFileAttachmentsOnboarding,
            attachmentsState = uiState.attachmentsState,
            canUseCustomFields = uiState.canPerformPaidAction,
            customFieldValidationErrors = uiState.errorList
                .filterIsInstance<CustomFieldValidationError>()
                .toPersistentList(),
            focusedField = uiState.focusedField.value()
        )
    }
}
