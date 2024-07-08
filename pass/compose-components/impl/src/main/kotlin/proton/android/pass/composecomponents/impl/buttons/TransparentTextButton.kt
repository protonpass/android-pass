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

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.captionStrongNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.defaultTint
import proton.android.pass.composecomponents.impl.icon.Icon

@Composable
fun TransparentTextButton(
    modifier: Modifier = Modifier,
    text: String,
    @DrawableRes prefixIcon: Int? = null,
    @DrawableRes suffixIcon: Int? = null,
    color: Color = defaultTint(),
    style: TextStyle = ProtonTheme.typography.captionStrongNorm.copy(fontSize = 14.sp),
    onClick: () -> Unit
) {
    Button(
        modifier = modifier,
        elevation = null,
        colors = ButtonDefaults.buttonColors(Color.Transparent),
        onClick = onClick
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall)) {
            if (prefixIcon != null) {
                Icon.Default(
                    id = prefixIcon,
                    modifier = Modifier.size(16.dp),
                    tint = color
                )
            }
            Text(
                text = text,
                style = style,
                color = color
            )
            if (suffixIcon != null) {
                Icon.Default(
                    id = suffixIcon,
                    modifier = Modifier.size(16.dp),
                    tint = color
                )
            }
        }
    }
}

@Preview
@Composable
fun TransparentTextButtonPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            TransparentTextButton(
                text = "A button",
                prefixIcon = me.proton.core.presentation.compose.R.drawable.ic_proton_plus,
                color = PassTheme.colors.interactionNormMajor2,
                onClick = {}
            )
        }
    }
}
