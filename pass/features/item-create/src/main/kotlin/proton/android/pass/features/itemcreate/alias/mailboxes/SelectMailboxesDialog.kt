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

package proton.android.pass.features.itemcreate.alias.mailboxes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import proton.android.pass.composecomponents.impl.dialogs.NoPaddingDialog
import proton.android.pass.features.itemcreate.alias.SelectedAliasMailboxUiModel

@Composable
internal fun SelectMailboxesDialog(
    modifier: Modifier = Modifier,
    mailboxes: ImmutableList<SelectedAliasMailboxUiModel>,
    canUpgrade: Boolean,
    color: Color,
    onMailboxesChanged: (List<SelectedAliasMailboxUiModel>) -> Unit,
    onDismiss: () -> Unit,
    viewModel: SelectMailboxesDialogViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.setMailboxes(mailboxes)
    }

    LaunchedEffect(canUpgrade) {
        viewModel.setCanUpgrade(canUpgrade)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    NoPaddingDialog(modifier = modifier, onDismissRequest = onDismiss) {
        SelectMailboxesDialogContent(
            state = uiState,
            color = color,
            onConfirm = { onMailboxesChanged(uiState.mailboxes) },
            onDismiss = onDismiss,
            onMailboxToggled = { viewModel.onMailboxChanged(it) }
        )
    }
}
