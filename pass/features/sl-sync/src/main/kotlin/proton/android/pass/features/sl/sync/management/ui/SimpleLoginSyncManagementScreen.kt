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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.features.sl.sync.management.presentation.SimpleLoginSyncManagementEvent
import proton.android.pass.features.sl.sync.management.presentation.SimpleLoginSyncManagementViewModel
import proton.android.pass.features.sl.sync.shared.navigation.SimpleLoginSyncNavDestination

@Composable
fun SimpleLoginSyncManagementScreen(
    onNavigated: (SimpleLoginSyncNavDestination) -> Unit,
    viewModel: SimpleLoginSyncManagementViewModel = hiltViewModel()
) = with(viewModel) {
    val state by state.collectAsStateWithLifecycle()

    LaunchedEffect(state.event) {
        when (state.event) {
            SimpleLoginSyncManagementEvent.Idle -> Unit
            SimpleLoginSyncManagementEvent.OnFetchAliasManagementError -> {
                SimpleLoginSyncNavDestination.CloseScreen(
                    force = true // Needed, otherwise navigation sometimes discards it as duplicated
                ).also(onNavigated)
            }
        }

        onConsumeEvent(event = state.event)
    }

    SimpleLoginSyncDetailsContent(
        state = state,
        onUiEvent = { uiEvent ->
            when (uiEvent) {
                SimpleLoginSyncManagementUiEvent.OnBackClicked -> {
                    onNavigated(SimpleLoginSyncNavDestination.CloseScreen())
                }

                SimpleLoginSyncManagementUiEvent.OnDomainClicked -> {
                    SimpleLoginSyncNavDestination.SelectDomain(
                        canSelectPremiumDomains = state.canManageAliases
                    ).also(onNavigated)
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

                SimpleLoginSyncManagementUiEvent.OnAddMailboxClicked -> {
                    onNavigated(SimpleLoginSyncNavDestination.CreateMailbox)
                }

                is SimpleLoginSyncManagementUiEvent.OnMailboxMenuClicked -> {
                    SimpleLoginSyncNavDestination.MailboxOptions(
                        mailboxId = uiEvent.aliasMailbox.id
                    ).also(onNavigated)
                }

                SimpleLoginSyncManagementUiEvent.OnUpsell -> {
                    onNavigated(SimpleLoginSyncNavDestination.Upsell)
                }
            }
        }
    )
}
