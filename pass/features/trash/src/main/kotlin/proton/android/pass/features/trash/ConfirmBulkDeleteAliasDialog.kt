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

package proton.android.pass.features.trash

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.dialogs.ConfirmWithLoadingDialog

@Composable
fun ConfirmBulkDeleteAliasDialog(
    show: Boolean,
    isLoading: Boolean,
    aliasCount: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    ConfirmWithLoadingDialog(
        show = show,
        isLoading = isLoading,
        isConfirmActionDestructive = true,
        title = stringResource(R.string.bulk_delete_alias_title, aliasCount),
        message = stringResource(R.string.bulk_delete_alias_subtitle),
        confirmText = stringResource(id = R.string.bulk_delete_alias_confirm),
        cancelText = stringResource(id = me.proton.core.presentation.R.string.presentation_alert_cancel),
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        onCancel = onDismiss
    )
}

@Preview
@Composable
fun ConfirmBulkDeleteAliasDialogPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            ConfirmBulkDeleteAliasDialog(
                show = true,
                aliasCount = 2,
                isLoading = false,
                onDismiss = {},
                onConfirm = {}
            )
        }
    }
}
