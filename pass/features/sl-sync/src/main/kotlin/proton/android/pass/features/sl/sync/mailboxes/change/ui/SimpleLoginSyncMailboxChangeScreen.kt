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

package proton.android.pass.features.sl.sync.mailboxes.change.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.features.sl.sync.mailboxes.change.presentation.SimpleLoginSyncMailboxChangeEvent
import proton.android.pass.features.sl.sync.mailboxes.change.presentation.SimpleLoginSyncMailboxChangeViewModel
import proton.android.pass.features.sl.sync.shared.navigation.SimpleLoginSyncNavDestination

@Composable
fun SimpleLoginSyncMailboxChangeScreen(
    onNavigated: (SimpleLoginSyncNavDestination) -> Unit,
    viewModel: SimpleLoginSyncMailboxChangeViewModel = hiltViewModel()
) = with(viewModel) {
    val state by stateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(state.event) {
        when (val event = state.event) {
            SimpleLoginSyncMailboxChangeEvent.Idle -> Unit
            is SimpleLoginSyncMailboxChangeEvent.OnMailboxChanged -> {
                if (!event.pendingEmail.value().isNullOrBlank()) {
                    SimpleLoginSyncNavDestination.VerifyMailbox(
                        mailboxId = event.mailboxId,
                        pendingEmail = event.pendingEmail
                    )
                } else {
                    SimpleLoginSyncNavDestination.BackToOrigin
                }.also(onNavigated)
            }
        }

        onConsumeEvent(event = state.event)
    }

    SimpleLoginSyncMailboxChangeContent(
        mailboxEmail = mailboxEmailState,
        state = state,
        onUiEvent = { uiEvent ->
            when (uiEvent) {
                SimpleLoginSyncMailboxChangeUiEvent.OnBackClicked -> {
                    SimpleLoginSyncNavDestination.CloseScreen()
                        .also(onNavigated)
                }

                SimpleLoginSyncMailboxChangeUiEvent.OnChangeClicked -> {
                    onChangeMailbox()
                }

                is SimpleLoginSyncMailboxChangeUiEvent.OnMailboxEmailChanged -> {
                    onMailboxEmailChanged(uiEvent.newMailboxEmail)
                }
            }
        }
    )
}
