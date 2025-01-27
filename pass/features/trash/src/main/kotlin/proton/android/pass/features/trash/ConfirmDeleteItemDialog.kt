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

package proton.android.pass.features.trash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.container.PassInfoWarningBanner
import proton.android.pass.composecomponents.impl.dialogs.ConfirmWithLoadingDialog
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
fun ConfirmDeleteItemDialog(
    modifier: Modifier = Modifier,
    show: Boolean,
    isLoading: Boolean,
    isSharedItem: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    ConfirmWithLoadingDialog(
        modifier = modifier,
        show = show,
        isLoading = isLoading,
        isConfirmActionDestructive = true,
        title = stringResource(id = R.string.alert_confirm_delete_item_dialog_title),
        confirmText = stringResource(id = CompR.string.action_continue),
        cancelText = stringResource(id = CompR.string.action_cancel),
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        onCancel = onDismiss,
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(space = Spacing.medium)
            ) {
                Text.Body1Regular(
                    text = stringResource(id = R.string.alert_confirm_delete_item_dialog_message)
                )

                if (isSharedItem) {
                    PassInfoWarningBanner(
                        text = stringResource(id = R.string.alert_confirm_delete_item_dialog_shared_warning_message),
                        backgroundColor = PassTheme.colors.interactionNormMinor2
                    )
                }
            }
        }
    )
}
