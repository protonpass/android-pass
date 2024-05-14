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
import proton.android.pass.featureitemcreate.impl.alias.AliasItemValidationErrors.BlankPrefix
import proton.android.pass.featureitemcreate.impl.alias.AliasItemValidationErrors.BlankTitle
import proton.android.pass.featureitemcreate.impl.alias.AliasItemValidationErrors.InvalidAliasContent
import proton.android.pass.featureitemcreate.impl.alias.mailboxes.SelectMailboxesDialog
import proton.android.pass.featureitemcreate.impl.alias.suffixes.SelectSuffixDialog
import proton.android.pass.featureitemcreate.impl.common.CreateUpdateTopBar

@OptIn(ExperimentalComposeUiApi::class)
@Composable
@Suppress("LongParameterList", "LongMethod")
internal fun AliasContent(
    modifier: Modifier = Modifier,
    uiState: BaseAliasUiState,
    aliasItemFormState: AliasItemFormState,
    selectedShareId: ShareId?,
    topBarActionName: String,
    isCreateMode: Boolean,
    isEditAllowed: Boolean,
    onUpClick: () -> Unit,
    onSubmit: (ShareId) -> Unit,
    onSuffixChange: (AliasSuffixUiModel) -> Unit,
    onMailboxesChanged: (List<SelectedAliasMailboxUiModel>) -> Unit,
    onNoteChange: (String) -> Unit,
    onPrefixChange: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onUpgrade: () -> Unit
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
                actionColor = PassTheme.colors.aliasInteractionNormMajor1,
                iconColor = PassTheme.colors.aliasInteractionNormMajor2,
                iconBackgroundColor = PassTheme.colors.aliasInteractionNormMinor1,
                onCloseClick = onUpClick,
                showUpgrade = uiState.hasReachedAliasLimit,
                onActionClick = { selectedShareId?.let(onSubmit) },
                onUpgrade = onUpgrade
            )
        }
    ) { padding ->
        CreateAliasForm(
            modifier = Modifier.padding(padding),
            aliasItemFormState = aliasItemFormState,
            isCreateMode = isCreateMode,
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
            onNoteChange = { onNoteChange(it) },
            onTitleChange = { onTitleChange(it) },
            onPrefixChange = { onPrefixChange(it) }
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
                    onSuffixChange(suffix)
                }
            },
            onDismiss = {
                scope.launch {
                    showSuffixDialog = false
                }
            },
            onUpgrade = onUpgrade
        )

        if (showMailboxDialog && aliasItemFormState.mailboxes.isNotEmpty()) {
            SelectMailboxesDialog(
                mailboxes = aliasItemFormState.mailboxes.toPersistentList(),
                color = PassTheme.colors.aliasInteractionNorm,
                canUpgrade = uiState.canUpgrade,
                onMailboxesChanged = {
                    showMailboxDialog = false
                    onMailboxesChanged(it)
                },
                onDismiss = { showMailboxDialog = false },
                onUpgrade = onUpgrade
            )
        }
    }
}
