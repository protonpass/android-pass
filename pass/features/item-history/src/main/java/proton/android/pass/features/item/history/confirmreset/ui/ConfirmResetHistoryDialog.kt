/*
 * Copyright (c) 2024-2025 Proton AG
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

package proton.android.pass.features.item.history.confirmreset.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.composecomponents.impl.dialogs.ConfirmWithLoadingDialog
import proton.android.pass.features.item.history.R
import proton.android.pass.features.item.history.confirmreset.presentation.ConfirmResetHistoryDialogEvent
import proton.android.pass.features.item.history.confirmreset.presentation.ConfirmResetHistoryDialogViewModel
import proton.android.pass.features.item.history.navigation.ItemHistoryNavDestination
import me.proton.core.presentation.R as CoreR

@Composable
fun ConfirmResetHistoryDialog(
    modifier: Modifier = Modifier,
    onNavigated: (ItemHistoryNavDestination) -> Unit,
    viewModel: ConfirmResetHistoryDialogViewModel = hiltViewModel()
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(state.event) {
        when (val event = state.event) {
            ConfirmResetHistoryDialogEvent.Idle -> {}
            ConfirmResetHistoryDialogEvent.OnError -> onNavigated(ItemHistoryNavDestination.Back)
            is ConfirmResetHistoryDialogEvent.OnSuccess ->
                onNavigated(ItemHistoryNavDestination.Detail(event.itemCategory))
        }

        viewModel.onConsumeEvent(state.event)
    }

    ConfirmWithLoadingDialog(
        modifier = modifier,
        show = true,
        title = stringResource(R.string.reset_history_dialog_title),
        isLoading = state.isLoading,
        isConfirmActionDestructive = true,
        isConfirmEnabled = !state.isLoading,
        message = stringResource(R.string.reset_history_dialog_body),
        confirmText = stringResource(R.string.reset_history_action),
        cancelText = stringResource(id = CoreR.string.presentation_alert_cancel),
        onConfirm = viewModel::onResetHistory,
        onDismiss = { onNavigated(ItemHistoryNavDestination.Back) },
        onCancel = { onNavigated(ItemHistoryNavDestination.Back) }
    )
}
