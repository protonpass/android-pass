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

package proton.android.pass.features.credentials.passkeys.creation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.dialogs.ConfirmWithLoadingDialog
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.features.credentials.R
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun PasskeyCredentialCreationConfirmDialog(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    itemUiModel: ItemUiModel,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ConfirmWithLoadingDialog(
        modifier = modifier,
        show = true,
        isLoading = isLoading,
        isConfirmActionDestructive = false,
        title = stringResource(R.string.passkey_credential_creation_confirmation_dialog_title),
        confirmText = stringResource(CompR.string.bottomsheet_confirm_button),
        cancelText = stringResource(CompR.string.bottomsheet_cancel_button),
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        onCancel = onDismiss,
        content = {
            Text.Body1Regular(
                text = stringResource(
                    id = R.string.passkey_credential_creation_confirmation_dialog_message,
                    itemUiModel.contents.title
                )
            )
        }
    )
}
