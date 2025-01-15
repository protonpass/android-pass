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

package proton.android.pass.features.item.trash.trashwarningshared.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.composecomponents.impl.dialogs.ConfirmWithLoadingDialog
import proton.android.pass.features.item.trash.R
import proton.android.pass.features.item.trash.trashwarningshared.presentation.ItemTrashWarningSharedState
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun ItemTrashWarningSharedContent(
    modifier: Modifier = Modifier,
    state: ItemTrashWarningSharedState,
    onUiEvent: (ItemTrashWarningSharedUiEvent) -> Unit
) = with(state) {
    ConfirmWithLoadingDialog(
        modifier = modifier,
        show = true,
        isLoading = isLoading,
        isConfirmActionDestructive = false,
        title = stringResource(id = R.string.item_trash_warning_shared_dialog_title),
        message = stringResource(id = R.string.item_trash_warning_shared_dialog_message),
        confirmText = stringResource(id = CompR.string.action_continue),
        cancelText = stringResource(id = CompR.string.action_cancel),
        onDismiss = {
            onUiEvent(ItemTrashWarningSharedUiEvent.OnDismissed)
        },
        onConfirm = {
            onUiEvent(ItemTrashWarningSharedUiEvent.OnContinueClicked)
        },
        onCancel = {
            onUiEvent(ItemTrashWarningSharedUiEvent.OnCancelClicked)
        }
    )
}
