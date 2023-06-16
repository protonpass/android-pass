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

@Composable
fun ConfirmCloseDialog(
    modifier: Modifier = Modifier,
    show: Boolean,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    if (!show) return

    ConfirmDialog(
        modifier = modifier,
        title = stringResource(R.string.confirm_close_dialog_title),
        message = stringResource(R.string.confirm_close_dialog_message),
        confirmText = stringResource(R.string.confirm_close_dialog_close_button),
        state = true,
        onDismiss = onCancel,
        onConfirm = { onConfirm() }
    )
}
