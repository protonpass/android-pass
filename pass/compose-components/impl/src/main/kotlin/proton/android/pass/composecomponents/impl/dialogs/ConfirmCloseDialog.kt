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

package proton.android.pass.composecomponents.impl.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
fun ConfirmCloseDialog(
    modifier: Modifier = Modifier,
    show: Boolean,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    ConfirmWithLoadingDialog(
        modifier = modifier,
        show = show,
        isLoading = false,
        isConfirmActionDestructive = false,
        title = stringResource(id = R.string.confirm_close_dialog_title),
        confirmText = stringResource(id = R.string.confirm_close_dialog_close_button),
        cancelText = stringResource(id = CompR.string.action_cancel),
        onDismiss = onCancel,
        onConfirm = onConfirm,
        onCancel = onCancel,
        content = {
            Text.Body1Regular(
                text = stringResource(id = R.string.confirm_close_dialog_message)
            )
        }
    )
}
