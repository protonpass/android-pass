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

package proton.android.pass.featureprofile.impl.manageaccountconfirmation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.composecomponents.impl.dialogs.ConfirmWithLoadingDialog
import proton.android.pass.featureprofile.impl.R
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
fun ManageAccountConfirmationContent(
    modifier: Modifier = Modifier,
    email: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ConfirmWithLoadingDialog(
        modifier = modifier,
        show = true,
        isLoading = false,
        isConfirmActionDestructive = false,
        title = stringResource(R.string.account_switch_confirmation_title),
        message = stringResource(R.string.account_switch_confirmation_body, email),
        confirmText = stringResource(R.string.account_switch_confirmation_confirm),
        cancelText = stringResource(CompR.string.action_cancel),
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        onCancel = onDismiss
    )
}
