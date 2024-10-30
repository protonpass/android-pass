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

package proton.android.pass.features.sl.sync.mailboxes.options.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.features.sl.sync.mailboxes.options.presentation.SimpleLoginSyncMailboxOptionsEvent
import proton.android.pass.features.sl.sync.mailboxes.options.presentation.SimpleLoginSyncMailboxOptionsViewModel
import proton.android.pass.features.sl.sync.shared.navigation.SimpleLoginSyncNavDestination

@Composable
fun SimpleLoginSyncMailboxOptionsBottomSheet(
    onNavigated: (SimpleLoginSyncNavDestination) -> Unit,
    viewModel: SimpleLoginSyncMailboxOptionsViewModel = hiltViewModel()
) = with(viewModel) {
    val state by stateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(state.event) {
        when (val event = state.event) {
            SimpleLoginSyncMailboxOptionsEvent.Idle -> Unit
            is SimpleLoginSyncMailboxOptionsEvent.OnDeleteMailbox -> {
                SimpleLoginSyncNavDestination.DeleteMailbox(
                    mailboxId = event.mailboxId
                ).also(onNavigated)
            }

            is SimpleLoginSyncMailboxOptionsEvent.OnMailboxVerifySuccess -> {
                SimpleLoginSyncNavDestination.VerifyMailbox(
                    mailboxId = event.mailboxId
                ).also(onNavigated)
            }

            SimpleLoginSyncMailboxOptionsEvent.OnMailboxOptionsError,
            SimpleLoginSyncMailboxOptionsEvent.OnMailboxSetAsDefaultError,
            SimpleLoginSyncMailboxOptionsEvent.OnMailboxSetAsDefaultSuccess,
            SimpleLoginSyncMailboxOptionsEvent.OnMailboxVerifyError -> {
                onNavigated(SimpleLoginSyncNavDestination.DismissBottomSheet)
            }
        }
    }

    SimpleLoginSyncMailboxOptionsContent(
        state = state,
        onUiEvent = { uiEvent ->
            when (uiEvent) {
                SimpleLoginSyncMailboxOptionsUiEvent.OnDeleteClicked -> {
                    onDeleteMailbox()
                }

                SimpleLoginSyncMailboxOptionsUiEvent.OnSetAsDefaultClicked -> {
                    onSetMailboxAsDefault()
                }

                SimpleLoginSyncMailboxOptionsUiEvent.OnVerifyClicked -> {
                    onVerifyMailbox()
                }
            }
        }
    )
}
