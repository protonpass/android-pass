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

package proton.android.pass.features.sl.sync.details.ui

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
import proton.android.pass.features.sl.sync.details.presentation.SimpleLoginSyncDetailsEvent
import proton.android.pass.features.sl.sync.details.presentation.SimpleLoginSyncDetailsViewModel
import proton.android.pass.features.sl.sync.details.ui.dialogs.SimpleLoginSyncDetailsOptionType
import proton.android.pass.features.sl.sync.shared.navigation.SimpleLoginSyncNavDestination

@Composable
fun SimpleLoginSyncDetailsScreen(
    onNavigated: (SimpleLoginSyncNavDestination) -> Unit,
    viewModel: SimpleLoginSyncDetailsViewModel = hiltViewModel()
) = with(viewModel) {
    val state by stateFlow.collectAsStateWithLifecycle()

    var dialogOptionTypeOption by remember {
        mutableStateOf<Option<SimpleLoginSyncDetailsOptionType>>(None)
    }

    LaunchedEffect(state.event) {
        when (state.event) {
            SimpleLoginSyncDetailsEvent.OnFetchAliasDetailsError -> {
                SimpleLoginSyncNavDestination.Back(
                    force = true // Needed, otherwise navigation sometimes discards it as duplicated
                ).also(onNavigated)
            }

            SimpleLoginSyncDetailsEvent.OnAliasDomainUpdated,
            SimpleLoginSyncDetailsEvent.OnAliasMailboxUpdated,
            SimpleLoginSyncDetailsEvent.OnUpdateAliasDomainError,
            SimpleLoginSyncDetailsEvent.OnUpdateAliasMailboxError -> {
                dialogOptionTypeOption = None
            }

            SimpleLoginSyncDetailsEvent.Idle -> Unit
        }

        onConsumeEvent(event = state.event)
    }

    SimpleLoginSyncDetailsContent(
        state = state,
        dialogOptionTypeOption = dialogOptionTypeOption,
        onUiEvent = { uiEvent ->
            when (uiEvent) {
                SimpleLoginSyncDetailsUiEvent.OnBackClicked -> {
                    onNavigated(SimpleLoginSyncNavDestination.Back())
                }

                SimpleLoginSyncDetailsUiEvent.OnDomainClicked -> {
                    dialogOptionTypeOption = SimpleLoginSyncDetailsOptionType.Domain.toOption()
                }

                SimpleLoginSyncDetailsUiEvent.OnMailboxClicked -> {
                    dialogOptionTypeOption = SimpleLoginSyncDetailsOptionType.Mailbox.toOption()
                }

                is SimpleLoginSyncDetailsUiEvent.OnSyncSettingsClicked -> {
                    SimpleLoginSyncNavDestination.Settings(
                        shareId = uiEvent.shareId
                    ).also(onNavigated)
                }

                is SimpleLoginSyncDetailsUiEvent.OnDefaultVaultClicked -> {
                    SimpleLoginSyncNavDestination.Settings(
                        shareId = uiEvent.shareId
                    ).also(onNavigated)
                }

                SimpleLoginSyncDetailsUiEvent.OnOptionsDialogDismissed -> {
                    when (dialogOptionTypeOption.value()) {
                        SimpleLoginSyncDetailsOptionType.Domain -> onRevertAliasDomainSelection()
                        SimpleLoginSyncDetailsOptionType.Mailbox -> onRevertAliasMailboxSelection()
                        else -> dialogOptionTypeOption = None
                    }
                }

                is SimpleLoginSyncDetailsUiEvent.OnDomainSelected -> {
                    onSelectAliasDomain(selectedAliasDomain = uiEvent.aliasDomain)
                }

                is SimpleLoginSyncDetailsUiEvent.OnMailboxSelected -> {
                    onSelectAliasMailbox(selectedAliasMailbox = uiEvent.aliasMailbox)
                }

                SimpleLoginSyncDetailsUiEvent.OnUpdateDomainClicked -> {
                    onUpdateAliasDomain()
                }

                SimpleLoginSyncDetailsUiEvent.OnUpdateMailboxClicked -> {
                    onUpdateAliasMailbox()
                }
            }
        }
    )
}
