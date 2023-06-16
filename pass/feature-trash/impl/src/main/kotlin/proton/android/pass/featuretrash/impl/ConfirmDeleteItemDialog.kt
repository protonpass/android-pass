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

package proton.android.pass.featuretrash.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import proton.android.pass.composecomponents.impl.dialogs.ConfirmWithLoadingDialog
import proton.android.pass.featuretrash.R

@Composable
fun ConfirmDeleteItemDialog(
    show: Boolean,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    ConfirmWithLoadingDialog(
        show = show,
        isLoading = isLoading,
        isConfirmActionDestructive = true,
        title = stringResource(R.string.alert_confirm_delete_item_dialog_title),
        message = stringResource(R.string.alert_confirm_delete_item_dialog_message),
        confirmText = stringResource(id = me.proton.core.presentation.R.string.presentation_alert_ok),
        cancelText = stringResource(id = me.proton.core.presentation.R.string.presentation_alert_cancel),
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        onCancel = onDismiss
    )
}
