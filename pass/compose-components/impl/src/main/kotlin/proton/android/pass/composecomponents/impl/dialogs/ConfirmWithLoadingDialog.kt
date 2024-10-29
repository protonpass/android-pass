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

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.window.DialogProperties
import me.proton.core.compose.component.ProtonAlertDialogText
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.headlineNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing

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
    extraButtons: List<@Composable () -> Unit> = emptyList(),
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
        extraButtons = extraButtons,
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
    extraButtons: List<(@Composable () -> Unit)> = emptyList(),
    confirmText: String,
    cancelText: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    LoadingDialog(
        modifier = modifier,
        show = show,
        title = title,
        content = content,
        onDismiss = onDismiss,
        buttons = buildList {
            add {
                DialogButton(
                    text = cancelText,
                    isEnabled = !isLoading,
                    onClick = onCancel
                )
            }
            addAll(extraButtons)
            add {
                DialogButton(
                    text = confirmText,
                    textColor = dialogConfirmColor(isConfirmEnabled, isConfirmActionDestructive),
                    isEnabled = isConfirmEnabled,
                    isLoading = isLoading,
                    onClick = onConfirm
                )
            }
        }
    )
}

@Composable
fun dialogConfirmColor(isConfirmEnabled: Boolean = true, isConfirmActionDestructive: Boolean) = if (isConfirmEnabled) {
    if (isConfirmActionDestructive) {
        PassTheme.colors.signalDanger
    } else {
        PassTheme.colors.interactionNormMajor2
    }
} else {
    PassTheme.colors.textDisabled
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoadingDialog(
    modifier: Modifier = Modifier,
    show: Boolean,
    title: String,
    content: @Composable () -> Unit,
    buttons: List<@Composable () -> Unit>,
    onDismiss: () -> Unit
) {
    if (!show) return
    BackHandler { onDismiss() }
    AlertDialog(
        modifier = modifier
            .fillMaxWidth(fraction = ALERT_DIALOG_WIDTH_FRACTION)
            .widthIn(max = MAX_ALERT_DIALOG_WIDTH),
        shape = ProtonTheme.shapes.medium,
        backgroundColor = ProtonTheme.colors.backgroundNorm,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        ),
        title = {
            Text(
                text = title,
                style = ProtonTheme.typography.headlineNorm,
                color = ProtonTheme.colors.textNorm
            )
        },
        text = content,
        onDismissRequest = onDismiss,
        buttons = {
            if (buttons.size > 2) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.mediumSmall),
                    horizontalAlignment = Alignment.End
                ) {
                    buttons.fastForEach { button -> button() }
                }
            } else {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.mediumSmall),
                    horizontalArrangement = Arrangement.End
                ) {
                    buttons.fastForEach { button -> button() }
                }
            }
        }
    )
}

private const val ALERT_DIALOG_WIDTH_FRACTION = 0.9f

// Mobile alert on desktop is 560dp wide
// https://material.io/components/dialogs#specs
private val MAX_ALERT_DIALOG_WIDTH = 560.dp
