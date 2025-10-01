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

package proton.android.pass.composecomponents.impl.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.proton.core.compose.component.ProtonButton
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider

@Composable
fun PassOutlinedButton(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = ProtonTheme.colors.brandNorm,
    style: TextStyle = LocalTextStyle.current,
    borderColor: Color = color,
    enabled: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    backgroundColor: Color = ProtonTheme.colors.backgroundNorm,
    shape: CornerBasedShape = ProtonTheme.shapes.medium,
    onClick: () -> Unit
) {
    ProtonButton(
        onClick = onClick,
        modifier = modifier,
        shape = shape,
        border = BorderStroke(ButtonDefaults.OutlinedBorderSize, borderColor),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = backgroundColor,
            contentColor = color
        ),
        elevation = null,
        contentPadding = PaddingValues(horizontal = 36.dp, vertical = Spacing.mediumSmall),
        enabled = enabled
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.W400,
            fontSize = 16.sp,
            color = color,
            maxLines = maxLines,
            style = style
        )
    }
}

@Preview
@Composable
fun PassOutlinedButtonPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            PassOutlinedButton(
                text = "This is an example button",
                onClick = {}
            )
        }
    }
}
