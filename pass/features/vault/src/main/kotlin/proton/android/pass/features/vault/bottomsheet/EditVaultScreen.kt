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

package proton.android.pass.features.vault.bottomsheet

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.features.vault.R
import proton.android.pass.features.vault.VaultNavigation
import proton.android.pass.features.vault.launchedeffects.InAppReviewTriggerLaunchedEffect

@Composable
fun EditVaultScreen(
    modifier: Modifier = Modifier,
    onNavigate: (VaultNavigation) -> Unit,
    viewModel: EditVaultViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    BackHandler {
        onNavigate(VaultNavigation.CloseScreen)
    }

    LaunchedEffect(Unit) {
        viewModel.onStart()
    }

    InAppReviewTriggerLaunchedEffect(
        triggerCondition = state.isVaultCreatedEvent is IsVaultCreatedEvent.Created
    )
    LaunchedEffect(state.isVaultCreatedEvent) {
        if (state.isVaultCreatedEvent == IsVaultCreatedEvent.Created) {
            onNavigate(VaultNavigation.CloseScreen)
        }
    }

    VaultContent(
        modifier = modifier,
        state = state,
        showUpgradeUi = false,
        buttonText = stringResource(R.string.bottomsheet_edit_vault_button),
        onNameChange = { viewModel.onNameChange(it) },
        onIconChange = { viewModel.onIconChange(it) },
        onColorChange = { viewModel.onColorChange(it) },
        onClose = { onNavigate(VaultNavigation.CloseScreen) },
        onButtonClick = { viewModel.onEditClick() },
        onUpgradeClick = {}
    )
}

