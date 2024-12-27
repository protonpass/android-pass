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

package proton.android.pass.features.attachments.deleteall.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.composecomponents.impl.dialogs.ConfirmWithLoadingDialog
import proton.android.pass.features.attachments.R
import proton.android.pass.features.attachments.deleteall.navigation.DeleteAllAttachmentsNavigation
import proton.android.pass.features.attachments.deleteall.presentation.DeleteAllAttachmentsEvent
import proton.android.pass.features.attachments.deleteall.presentation.DeleteAllAttachmentsViewModel
import me.proton.core.presentation.R as CoreR

@Composable
fun DeleteAllAttachmentsDialog(
    modifier: Modifier = Modifier,
    viewModel: DeleteAllAttachmentsViewModel = hiltViewModel(),
    onNavigate: (DeleteAllAttachmentsNavigation) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.event) {
        when (state.event) {
            DeleteAllAttachmentsEvent.Close -> onNavigate(DeleteAllAttachmentsNavigation.CloseDialog)
            DeleteAllAttachmentsEvent.Idle -> {}
        }
        viewModel.onConsumeEvent(state.event)
    }
    ConfirmWithLoadingDialog(
        modifier = modifier,
        show = true,
        isLoading = state.isDeleting,
        isConfirmActionDestructive = true,
        title = stringResource(R.string.delete_all_attachments_title),
        message = stringResource(R.string.delete_all_attachments_subtitle),
        confirmText = stringResource(R.string.delete_all_attachments_confirm),
        cancelText = stringResource(id = CoreR.string.presentation_alert_cancel),
        onDismiss = {
            onNavigate(DeleteAllAttachmentsNavigation.CloseDialog)
        },
        onConfirm = {
            viewModel.deleteAllAttachments()
        },
        onCancel = {
            onNavigate(DeleteAllAttachmentsNavigation.CloseDialog)
        }
    )
}
