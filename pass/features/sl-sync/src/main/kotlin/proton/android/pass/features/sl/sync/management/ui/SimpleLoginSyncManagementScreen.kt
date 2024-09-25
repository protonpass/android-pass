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

package proton.android.pass.features.sl.sync.management.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.features.sl.sync.management.presentation.SimpleLoginSyncManagementEvent
import proton.android.pass.features.sl.sync.management.presentation.SimpleLoginSyncManagementViewModel
import proton.android.pass.features.sl.sync.shared.navigation.SimpleLoginSyncNavDestination

@Composable
fun SimpleLoginSyncDetailsScreen(
    onNavigated: (SimpleLoginSyncNavDestination) -> Unit,
    viewModel: SimpleLoginSyncManagementViewModel = hiltViewModel()
) = with(viewModel) {
    val state by state.collectAsStateWithLifecycle()

    var shouldShowAliasDomainDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.event) {
        when (state.event) {
            SimpleLoginSyncManagementEvent.OnFetchAliasManagementError -> {
                SimpleLoginSyncNavDestination.Back(
                    force = true // Needed, otherwise navigation sometimes discards it as duplicated
                ).also(onNavigated)
            }

            SimpleLoginSyncManagementEvent.OnAliasDomainUpdated,
            SimpleLoginSyncManagementEvent.OnAliasMailboxUpdated,
            SimpleLoginSyncManagementEvent.OnUpdateAliasDomainError,
            SimpleLoginSyncManagementEvent.OnUpdateAliasMailboxError -> {
                shouldShowAliasDomainDialog = false
            }

            SimpleLoginSyncManagementEvent.Idle -> {}
        }

        onConsumeEvent(event = state.event)
    }

    SimpleLoginSyncDetailsContent(
        state = state,
        shouldShowAliasDomainDialog = shouldShowAliasDomainDialog,
        onUiEvent = { uiEvent ->
            when (uiEvent) {
                SimpleLoginSyncManagementUiEvent.OnBackClicked -> {
                    onNavigated(SimpleLoginSyncNavDestination.Back())
                }

                SimpleLoginSyncManagementUiEvent.OnDomainClicked -> {
                    shouldShowAliasDomainDialog = true
                }

                is SimpleLoginSyncManagementUiEvent.OnSyncSettingsClicked -> {
                    SimpleLoginSyncNavDestination.Settings(
                        shareId = uiEvent.shareId
                    ).also(onNavigated)
                }

                is SimpleLoginSyncManagementUiEvent.OnDefaultVaultClicked -> {
                    SimpleLoginSyncNavDestination.Settings(
                        shareId = uiEvent.shareId
                    ).also(onNavigated)
                }

                SimpleLoginSyncManagementUiEvent.OnOptionsDialogDismissed -> {
                    shouldShowAliasDomainDialog = false
                }

                is SimpleLoginSyncManagementUiEvent.OnDomainSelected -> {
                    onSelectAliasDomain(selectedAliasDomain = uiEvent.aliasDomain)
                }

                SimpleLoginSyncManagementUiEvent.OnUpdateDomainClicked -> {
                    onUpdateAliasDomain()
                }

                SimpleLoginSyncManagementUiEvent.OnAddMailboxClicked -> {
                    // Will be implemented in IDTEAM-3911
                }

                is SimpleLoginSyncManagementUiEvent.OnMailboxMenuClicked -> {
                    // Will be implemented in IDTEAM-3911
                }
            }
        }
    )
}
