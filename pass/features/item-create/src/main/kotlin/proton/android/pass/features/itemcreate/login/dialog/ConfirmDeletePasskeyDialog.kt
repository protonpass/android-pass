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

package proton.android.pass.features.itemcreate.login.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.commonuimodels.api.UIPasskeyContent
import proton.android.pass.composecomponents.impl.dialogs.ConfirmWithLoadingDialog
import proton.android.pass.features.itemcreate.R
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
fun ConfirmDeletePasskeyDialog(
    modifier: Modifier = Modifier,
    passkey: UIPasskeyContent,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    ConfirmWithLoadingDialog(
        modifier = modifier,
        show = true,
        isLoading = false,
        isConfirmActionDestructive = true,
        title = stringResource(R.string.remove_passkey_dialog_title),
        message = stringResource(
            id = R.string.remove_passkey_dialog_body,
            passkey.userName,
            passkey.domain
        ),
        confirmText = stringResource(R.string.action_remove),
        cancelText = stringResource(CompR.string.bottomsheet_cancel_button),
        onDismiss = onCancel,
        onConfirm = onConfirm,
        onCancel = onCancel
    )
}

