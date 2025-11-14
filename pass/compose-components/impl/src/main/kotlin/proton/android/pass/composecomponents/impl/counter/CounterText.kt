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

package proton.android.pass.composecomponents.impl.counter

import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.container.Pill

@Composable
fun CounterText(
    modifier: Modifier = Modifier,
    text: String,
    backgroundColor: Color,
    textColor: Color,
    textStyle: TextStyle = ProtonTheme.typography.defaultNorm
) {
    Pill(
        modifier = modifier,
        backgroundColor = backgroundColor
    ) {
        Text(
            text = text,
            color = textColor,
            style = textStyle
        )
    }
}

@Preview
@Composable
fun CounterTextPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            CounterText(
                text = "123456",
                backgroundColor = PassTheme.colors.passwordInteractionNormMajor2,
                textColor = PassTheme.colors.passwordInteractionNormMinor1
            )
        }
    }
}
