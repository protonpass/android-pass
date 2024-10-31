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

package proton.android.pass.features.sl.sync.mailboxes.verify.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.features.sl.sync.mailboxes.verify.presentation.SimpleLoginSyncMailboxVerifyEvent
import proton.android.pass.features.sl.sync.mailboxes.verify.presentation.SimpleLoginSyncMailboxVerifyViewModel
import proton.android.pass.features.sl.sync.shared.navigation.SimpleLoginSyncNavDestination

@Composable
fun SimpleLoginSyncMailboxVerifyScreen(
    onNavigated: (SimpleLoginSyncNavDestination) -> Unit,
    viewModel: SimpleLoginSyncMailboxVerifyViewModel = hiltViewModel()
) = with(viewModel) {
    val state by stateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = state.event) {
        when (state.event) {
            SimpleLoginSyncMailboxVerifyEvent.Idle -> Unit
            SimpleLoginSyncMailboxVerifyEvent.OnVerifyAliasMailboxCompleted -> {
                onNavigated(SimpleLoginSyncNavDestination.AliasManagement)
            }
        }

        onConsumeEvent(event = state.event)
    }

    SimpleLoginSyncMailboxVerifyContent(
        verificationCode = verificationCodeState,
        state = state,
        onUiEvent = { uiEvent ->
            when (uiEvent) {
                SimpleLoginSyncMailboxVerifyUiEvent.OnCloseClicked -> {
                    onNavigated(SimpleLoginSyncNavDestination.AliasManagement)
                }

                SimpleLoginSyncMailboxVerifyUiEvent.OnVerifyClicked -> {
                    onVerifyAliasMailbox()
                }

                is SimpleLoginSyncMailboxVerifyUiEvent.OnVerificationCodeChanged -> {
                    onVerificationCodeChanged(newVerificationCode = uiEvent.newVerificationCode)
                }

                SimpleLoginSyncMailboxVerifyUiEvent.OnResendVerificationCodeClicked -> {
                    onResendVerificationCode()
                }
            }
        }
    )
}
