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

package proton.android.pass.featuretrash.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import proton.android.pass.composecomponents.impl.dialogs.ConfirmWithLoadingDialog
import proton.android.pass.featuretrash.R
import me.proton.core.presentation.R as CoreR

@Composable
fun ConfirmTrashAliasDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    ConfirmWithLoadingDialog(
        show = show,
        isLoading = false,
        isConfirmActionDestructive = true,
        title = stringResource(R.string.alias_dialog_move_to_trash_title),
        message = stringResource(R.string.alias_dialog_move_to_trash_content),
        confirmText = stringResource(id = R.string.alias_dialog_move_to_trash_confirm),
        cancelText = stringResource(id = CoreR.string.presentation_alert_cancel),
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        onCancel = onDismiss
    )
}
