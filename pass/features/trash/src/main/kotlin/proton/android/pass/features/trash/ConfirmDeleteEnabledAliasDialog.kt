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

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.component.ProtonAlertDialogText
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.dialogs.DialogButton
import proton.android.pass.composecomponents.impl.dialogs.LoadingDialog
import proton.android.pass.composecomponents.impl.dialogs.dialogConfirmColor
import me.proton.core.presentation.R as CoreR

@Composable
fun ConfirmDeleteEnabledAliasDialog(
    modifier: Modifier = Modifier,
    show: Boolean,
    isDeleteLoading: Boolean,
    isDisableLoading: Boolean,
    alias: String,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onDisable: () -> Unit
) {
    LoadingDialog(
        modifier = modifier,
        title = stringResource(R.string.delete_alias_title, alias),
        show = show,
        onDismiss = onDismiss,
        content = {
            ProtonAlertDialogText(
                text = stringResource(R.string.delete_alias_enabled_subtitle)
            )
        },
        buttons = buildList {
            val isAnyLoading = isDeleteLoading || isDisableLoading
            add {
                DialogButton(
                    text = stringResource(R.string.delete_alias_confirm),
                    textColor = dialogConfirmColor(!isDeleteLoading, true),
                    isEnabled = !isAnyLoading,
                    isLoading = isDeleteLoading,
                    onClick = onDelete
                )
            }
            add {
                DialogButton(
                    text = stringResource(R.string.delete_alias_disable),
                    textColor = dialogConfirmColor(!isDisableLoading, false),
                    isEnabled = !isAnyLoading,
                    isLoading = isDisableLoading,
                    onClick = onDisable
                )
            }
            add {
                DialogButton(
                    text = stringResource(id = CoreR.string.presentation_alert_cancel),
                    isEnabled = !isAnyLoading,
                    onClick = onDismiss
                )
            }
        }
    )
}

@Preview
@Composable
fun ConfirmDeleteEnabledAliasDialogPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            ConfirmDeleteEnabledAliasDialog(
                show = true,
                alias = "MyAlias",
                isDeleteLoading = false,
                isDisableLoading = false,
                onDismiss = {},
                onDelete = {},
                onDisable = {}
            )
        }
    }
}
