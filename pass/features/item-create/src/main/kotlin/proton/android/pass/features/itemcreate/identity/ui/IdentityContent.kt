/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.features.itemcreate.identity.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.features.itemcreate.common.CreateUpdateTopBar
import proton.android.pass.features.itemcreate.identity.navigation.IdentityContentEvent
import proton.android.pass.features.itemcreate.identity.presentation.IdentityItemFormState
import proton.android.pass.features.itemcreate.identity.presentation.IdentityUiState

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun IdentityContent(
    modifier: Modifier = Modifier,
    identityItemFormState: IdentityItemFormState,
    topBarActionName: String,
    identityUiState: IdentityUiState,
    canUseAttachments: Boolean,
    onEvent: (IdentityContentEvent) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CreateUpdateTopBar(
                text = topBarActionName,
                isLoading = identityUiState.getSubmitLoadingState().value(),
                actionColor = PassTheme.colors.interactionNormMajor1,
                iconColor = PassTheme.colors.interactionNormMajor2,
                iconBackgroundColor = PassTheme.colors.interactionNormMinor1,
                selectedVault = identityUiState.getSelectedVault().value(),
                showVaultSelector = identityUiState.shouldShowVaultSelector(),
                onCloseClick = { onEvent(IdentityContentEvent.Up) },
                onActionClick = {
                    when (val selectedShareId = identityUiState.getSelectedShareId()) {
                        None -> return@CreateUpdateTopBar
                        is Some -> onEvent(IdentityContentEvent.Submit(selectedShareId.value))
                    }
                },
                onUpgrade = { },
                onVaultSelectorClick = {
                    when (val selectedShareId = identityUiState.getSelectedShareId()) {
                        None -> return@CreateUpdateTopBar
                        is Some -> onEvent(IdentityContentEvent.OnVaultSelect(selectedShareId.value))
                    }
                }
            )
        }
    ) { padding ->
        IdentityItemForm(
            modifier = Modifier.padding(padding),
            identityItemFormState = identityItemFormState,
            identityUiState = identityUiState,
            canUseAttachments = canUseAttachments,
            onEvent = onEvent
        )
    }
}
