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

package proton.android.pass.features.attachments.renameattachment.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.composecomponents.impl.dialogs.NoPaddingDialog
import proton.android.pass.composecomponents.impl.dialogs.SingleInputDialogContent
import proton.android.pass.features.attachments.R
import proton.android.pass.features.attachments.renameattachment.navigation.RenameAttachmentNavigation
import proton.android.pass.features.attachments.renameattachment.presentation.RenameAttachmentEvent
import proton.android.pass.features.attachments.renameattachment.presentation.RenameAttachmentViewModel

@Composable
fun RenameAttachmentDialog(
    modifier: Modifier = Modifier,
    viewmodel: RenameAttachmentViewModel = hiltViewModel(),
    onNavigate: (RenameAttachmentNavigation) -> Unit
) {
    val state by viewmodel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state) {
        when (state) {
            RenameAttachmentEvent.Close -> onNavigate(RenameAttachmentNavigation.CloseDialog)
            RenameAttachmentEvent.Idle -> {}
        }
        viewmodel.onConsumeEvent(state)
    }

    NoPaddingDialog(
        modifier = modifier,
        onDismissRequest = { onNavigate(RenameAttachmentNavigation.CloseDialog) }
    ) {
        SingleInputDialogContent(
            value = viewmodel.filename,
            canConfirm = viewmodel.filename.isNotEmpty(),
            titleRes = R.string.rename_attachment_dialog_title,
            onChange = viewmodel::onValueChange,
            onConfirm = viewmodel::onConfirm,
            onCancel = { onNavigate(RenameAttachmentNavigation.CloseDialog) }
        )
    }
}

