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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import proton.android.pass.commonui.api.PassTheme

@Composable
fun NoPaddingDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    shape: Shape = MaterialTheme.shapes.medium,
    backgroundColor: Color = PassTheme.colors.backgroundStrong,
    contentColor: Color = contentColorFor(backgroundColor),
    properties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        Surface(
            modifier = modifier.fixAlertDialogSize(),
            shape = shape,
            color = backgroundColor,
            contentColor = contentColor
        ) {
            content()
        }
    }
}


private fun Modifier.fixAlertDialogSize() = fillMaxWidth(fraction = ALERT_DIALOG_WIDTH_FRACTION)
    .widthIn(max = MaxAlertDialogWidth)

private const val ALERT_DIALOG_WIDTH_FRACTION = 0.9f

// Mobile alert on desktop is 560dp wide
// https://material.io/components/dialogs#specs
private val MaxAlertDialogWidth = 560.dp
