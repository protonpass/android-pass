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
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.android.pass.features.sl.sync.management.presentation.SimpleLoginSyncManagementEvent
import proton.android.pass.features.sl.sync.management.presentation.SimpleLoginSyncManagementViewModel
import proton.android.pass.features.sl.sync.management.ui.dialogs.SimpleLoginSyncManagementOptionType
import proton.android.pass.features.sl.sync.shared.navigation.SimpleLoginSyncNavDestination

@Composable
fun SimpleLoginSyncDetailsScreen(
    onNavigated: (SimpleLoginSyncNavDestination) -> Unit,
    viewModel: SimpleLoginSyncManagementViewModel = hiltViewModel()
) = with(viewModel) {
    val state by state.collectAsStateWithLifecycle()

    var dialogOptionTypeOption by remember {
        mutableStateOf<Option<SimpleLoginSyncManagementOptionType>>(None)
    }

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
                dialogOptionTypeOption = None
            }

            SimpleLoginSyncManagementEvent.Idle -> {}
        }

        onConsumeEvent(event = state.event)
    }

    SimpleLoginSyncDetailsContent(
        state = state,
        dialogOptionTypeOption = dialogOptionTypeOption,
        onUiEvent = { uiEvent ->
            when (uiEvent) {
                SimpleLoginSyncManagementUiEvent.OnBackClicked -> {
                    onNavigated(SimpleLoginSyncNavDestination.Back())
                }

                SimpleLoginSyncManagementUiEvent.OnDomainClicked -> {
                    dialogOptionTypeOption = SimpleLoginSyncManagementOptionType.Domain.toOption()
                }

                SimpleLoginSyncManagementUiEvent.OnMailboxClicked -> {
                    dialogOptionTypeOption = SimpleLoginSyncManagementOptionType.Mailbox.toOption()
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
                    dialogOptionTypeOption = None
                }

                is SimpleLoginSyncManagementUiEvent.OnDomainSelected -> {
                    onSelectAliasDomain(selectedAliasDomain = uiEvent.aliasDomain)
                }

                is SimpleLoginSyncManagementUiEvent.OnMailboxSelected -> {
                    onSelectAliasMailbox(selectedAliasMailbox = uiEvent.aliasMailbox)
                }

                SimpleLoginSyncManagementUiEvent.OnUpdateDomainClicked -> {
                    onUpdateAliasDomain()
                }

                SimpleLoginSyncManagementUiEvent.OnUpdateMailboxClicked -> {
                    onUpdateAliasMailbox()
                }
            }
        }
    )
}
