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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.component.ProtonTextButton
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import me.proton.core.presentation.compose.R as CoreR

@Composable
fun DialogCancelConfirmSection(
    modifier: Modifier = Modifier,
    cancelText: String = stringResource(CoreR.string.presentation_alert_cancel),
    confirmText: String = stringResource(CoreR.string.presentation_alert_ok),
    color: Color,
    disabledColor: Color = color,
    confirmEnabled: Boolean = true,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val confirmColor = if (confirmEnabled) color else disabledColor
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        ProtonTextButton(onClick = onDismiss) {
            Text(
                text = cancelText,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        ProtonTextButton(
            enabled = confirmEnabled,
            onClick = onConfirm
        ) {
            Text(
                text = confirmText,
                color = confirmColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview
@Composable
fun DialogCancelConfirmSectionPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    PassTheme(isDark = input.first) {
        Surface {
            DialogCancelConfirmSection(
                color = PassTheme.colors.loginInteractionNorm,
                confirmEnabled = input.second,
                onConfirm = {},
                onDismiss = {}
            )
        }
    }
}
