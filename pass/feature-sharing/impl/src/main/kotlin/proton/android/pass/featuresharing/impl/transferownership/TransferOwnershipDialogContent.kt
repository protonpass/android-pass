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

package proton.android.pass.featuresharing.impl.transferownership

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.composecomponents.impl.dialogs.ConfirmWithLoadingDialog
import proton.android.pass.featuresharing.impl.R
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
fun TransferOwnershipDialogContent(
    modifier: Modifier = Modifier,
    state: TransferOwnershipState,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    ConfirmWithLoadingDialog(
        modifier = modifier,
        show = true,
        isLoading = state.isLoadingState.value(),
        isConfirmActionDestructive = true,
        title = stringResource(R.string.sharing_transfer_ownership_confirm_title),
        message = stringResource(R.string.sharing_transfer_ownership_confirm_message, state.memberEmail),
        confirmText = stringResource(CompR.string.bottomsheet_confirm_button),
        cancelText = stringResource(CompR.string.bottomsheet_cancel_button),
        onDismiss = onCancel,
        onConfirm = onConfirm,
        onCancel = onCancel
    )

}
