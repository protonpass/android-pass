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

package proton.android.pass.features.sl.sync.mailboxes.delete.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.features.sl.sync.mailboxes.delete.presentation.SimpleLoginSyncMailboxDeleteEvent
import proton.android.pass.features.sl.sync.mailboxes.delete.presentation.SimpleLoginSyncMailboxDeleteViewModel
import proton.android.pass.features.sl.sync.mailboxes.delete.ui.dialogs.SimpleLoginSyncMailboxTransferDialog
import proton.android.pass.features.sl.sync.shared.navigation.SimpleLoginSyncNavDestination

@Composable
fun SimpleLoginSyncMailboxDeleteBottomSheet(
    modifier: Modifier = Modifier,
    onNavigated: (SimpleLoginSyncNavDestination) -> Unit,
    viewModel: SimpleLoginSyncMailboxDeleteViewModel = hiltViewModel()
) = with(viewModel) {
    val state by stateFlow.collectAsStateWithLifecycle()
    var showTransferAliasesDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.event) {
        when (state.event) {
            SimpleLoginSyncMailboxDeleteEvent.Idle -> Unit

            SimpleLoginSyncMailboxDeleteEvent.OnDeleteAliasMailboxError,
            SimpleLoginSyncMailboxDeleteEvent.OnDeleteAliasMailboxSuccess -> {
                onNavigated(SimpleLoginSyncNavDestination.DismissBottomSheet)
            }
        }
    }

    SimpleLoginSyncMailboxDeleteContent(
        modifier = modifier,
        state = state,
        onUiEvent = { uiEvent ->
            when (uiEvent) {
                SimpleLoginSyncMailboxDeleteUiEvent.OnCancelClicked -> {
                    onNavigated(SimpleLoginSyncNavDestination.DismissBottomSheet)
                }

                SimpleLoginSyncMailboxDeleteUiEvent.OnDeleteClicked -> {
                    onDeleteMailbox()
                }

                SimpleLoginSyncMailboxDeleteUiEvent.OnSelectAliasMailboxClicked -> {
                    showTransferAliasesDialog = true
                }

                is SimpleLoginSyncMailboxDeleteUiEvent.OnTransferAliasesToggled -> {
                    onToggleTransferAliases(uiEvent.isTransferAliasesEnabled)
                }
            }
        }
    )

    if (showTransferAliasesDialog) {
        SimpleLoginSyncMailboxTransferDialog(
            selectedTransferAliasMailboxId = state.transferAliasMailboxId,
            transferAliasMailboxes = state.transferAliasMailboxes.toPersistentList(),
            onTransferAliasMailboxSelected = { selectedAliasMailbox ->
                showTransferAliasesDialog = false
                onSelectTransferAliasMailbox(selectedAliasMailbox)
            },
            onDismiss = { showTransferAliasesDialog = false }
        )
    }
}
