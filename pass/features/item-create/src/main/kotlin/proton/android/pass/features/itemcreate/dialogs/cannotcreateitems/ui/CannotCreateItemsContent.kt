/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.itemcreate.dialogs.cannotcreateitems.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.composecomponents.impl.dialogs.ConfirmWithLoadingDialog
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.features.itemcreate.R
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun CannotCreateItemsContent(modifier: Modifier = Modifier, onUiEvent: (CannotCreateItemsUiEvent) -> Unit) {
    ConfirmWithLoadingDialog(
        modifier = modifier,
        show = true,
        isLoading = false,
        isConfirmActionDestructive = false,
        title = stringResource(id = R.string.cannot_create_items_dialog_title),
        confirmText = stringResource(id = CompR.string.action_close),
        cancelText = "",
        onDismiss = {
            onUiEvent(CannotCreateItemsUiEvent.OnDismissed)
        },
        onConfirm = {
            onUiEvent(CannotCreateItemsUiEvent.OnClosed)
        },
        onCancel = {
            onUiEvent(CannotCreateItemsUiEvent.OnClosed)
        },
        content = {
            Text.Body1Regular(
                text = stringResource(id = R.string.cannot_create_items_dialog_message)
            )
        }
    )
}
