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

package proton.android.pass.composecomponents.impl.dialogs

import androidx.compose.foundation.layout.height
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.proton.core.compose.component.ProtonAlertDialog
import me.proton.core.compose.component.ProtonAlertDialogText
import me.proton.core.compose.component.ProtonTextButton
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.headlineNorm

@Composable
fun ConfirmWithLoadingDialog(
    modifier: Modifier = Modifier,
    show: Boolean,
    isLoading: Boolean,
    isConfirmActionDestructive: Boolean,
    isConfirmEnabled: Boolean = true,
    title: String,
    message: String,
    confirmText: String,
    cancelText: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    ConfirmWithLoadingDialog(
        modifier = modifier,
        show = show,
        isLoading = isLoading,
        isConfirmActionDestructive = isConfirmActionDestructive,
        isConfirmEnabled = isConfirmEnabled,
        title = title,
        content = {
            ProtonAlertDialogText(
                text = message
            )
        },
        confirmText = confirmText,
        cancelText = cancelText,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        onCancel = onCancel
    )
}

@Composable
fun ConfirmWithLoadingDialog(
    modifier: Modifier = Modifier,
    show: Boolean,
    isLoading: Boolean,
    isConfirmActionDestructive: Boolean,
    isConfirmEnabled: Boolean = true,
    title: String,
    content: @Composable () -> Unit,
    confirmText: String,
    cancelText: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    if (!show) return

    val confirmColor = if (isConfirmEnabled) {
        if (isConfirmActionDestructive) {
            ProtonTheme.colors.notificationError
        } else {
            ProtonTheme.colors.brandNorm
        }
    } else {
        ProtonTheme.colors.textDisabled
    }

    ProtonAlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = ProtonTheme.typography.headlineNorm,
                color = ProtonTheme.colors.textNorm
            )
        },
        text = content,
        confirmButton = {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.height(48.dp)
                )
            } else {
                ProtonTextButton(
                    onClick = onConfirm,
                    enabled = isConfirmEnabled,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Transparent,
                        disabledBackgroundColor = Color.Transparent,
                        contentColor = confirmColor
                    )
                ) {
                    Text(
                        text = confirmText,
                        color = confirmColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        dismissButton = {
            if (!isLoading) {
                ProtonTextButton(onClick = onCancel) {
                    Text(
                        text = cancelText,
                        color = ProtonTheme.colors.brandNorm,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    )
}

