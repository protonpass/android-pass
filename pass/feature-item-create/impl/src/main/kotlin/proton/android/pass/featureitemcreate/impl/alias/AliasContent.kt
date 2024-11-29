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

package proton.android.pass.featureitemcreate.impl.alias

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault
import proton.android.pass.featureitemcreate.impl.alias.AliasItemValidationErrors.BlankPrefix
import proton.android.pass.featureitemcreate.impl.alias.AliasItemValidationErrors.BlankTitle
import proton.android.pass.featureitemcreate.impl.alias.AliasItemValidationErrors.InvalidAliasContent
import proton.android.pass.featureitemcreate.impl.alias.mailboxes.SelectMailboxesDialog
import proton.android.pass.featureitemcreate.impl.alias.suffixes.SelectSuffixDialog
import proton.android.pass.featureitemcreate.impl.common.CreateUpdateTopBar

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
    onEvent: (AliasContentUiEvent) -> Unit
) {
    val scope = rememberCoroutineScope()

    var showMailboxDialog by rememberSaveable { mutableStateOf(false) }
    var showSuffixDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            CreateUpdateTopBar(
                text = topBarActionName,
                isLoading = uiState.isLoadingState.value(),
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
        CreateAliasForm(
            modifier = Modifier.padding(padding),
            aliasItemFormState = aliasItemFormState,
            isCreateMode = isCreateMode,
            isAliasCreatedByUser = isAliasCreatedByUser,
            isEditAllowed = isEditAllowed,
            isLoading = uiState.isLoadingState.value(),
            showUpgrade = uiState.hasReachedAliasLimit,
            onTitleRequiredError = uiState.errorList.contains(BlankTitle),
            onAliasRequiredError = uiState.errorList.contains(BlankPrefix),
            onInvalidAliasError = uiState.errorList.contains(InvalidAliasContent),
            onSuffixClick = {
                scope.launch {
                    showSuffixDialog = true
                }
            },
            onMailboxClick = {
                scope.launch {
                    showMailboxDialog = true
                }
            },
            onEvent = onEvent,
            isAliasManagementEnabled = uiState.isAliasManagementEnabled,
            isFileAttachmentsEnabled = uiState.isFileAttachmentEnabled,
            attachmentList = emptyList()
        )

        SelectSuffixDialog(
            show = showSuffixDialog,
            canUpgrade = false,
            suffixes = aliasItemFormState.aliasOptions.suffixes.toImmutableList(),
            selectedSuffix = aliasItemFormState.selectedSuffix,
            color = PassTheme.colors.aliasInteractionNorm,
            onSuffixChanged = { suffix ->
                scope.launch {
                    showSuffixDialog = false
                    onEvent(AliasContentUiEvent.OnSuffixChanged(suffix))
                }
            },
            onDismiss = {
                scope.launch {
                    showSuffixDialog = false
                }
            },
            onUpgrade = { onEvent(AliasContentUiEvent.OnUpgrade) }
        )

        if (showMailboxDialog && aliasItemFormState.mailboxes.isNotEmpty()) {
            SelectMailboxesDialog(
                mailboxes = aliasItemFormState.mailboxes.toPersistentList(),
                color = PassTheme.colors.aliasInteractionNorm,
                canUpgrade = uiState.canUpgrade,
                onMailboxesChanged = {
                    showMailboxDialog = false
                    onEvent(AliasContentUiEvent.OnMailBoxChanged(it))
                },
                onDismiss = { showMailboxDialog = false }
            )
        }
    }
}
