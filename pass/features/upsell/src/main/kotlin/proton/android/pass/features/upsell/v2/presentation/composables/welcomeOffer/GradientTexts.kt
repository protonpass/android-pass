/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.upsell.v2.presentation.composables.welcomeOffer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.LocalDark
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.text.Text

private val verticalGradientDark = listOf(
    Color(color = 0xFFFDD382), Color(color = 0xFFE3AAAA)
)

private val verticalGradientLight = listOf(
    Color(color = 0xFF704CFF), Color(color = 0xFF9C63E8)
)

@Composable
internal fun GradientTextLimited(modifier: Modifier = Modifier, text: String) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = if (LocalDark.current) {
                        verticalGradientDark
                    } else {
                        verticalGradientLight
                    }
                )
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text.Body3Bold(
            color = ProtonTheme.colors.textInverted,
            text = text
        )
    }
}


@Composable
internal fun GradientText(
    modifier: Modifier = Modifier,
    text: String,
    textSize: Float // pixel
) {
    Text(
        modifier = modifier,
        text = text,
        style = ProtonTheme.typography.hero.copy(
            fontSize = (textSize / LocalDensity.current.fontScale).sp,
            brush = Brush.verticalGradient(
                colors = if (LocalDark.current) {
                    verticalGradientDark
                } else {
                    verticalGradientLight
                }
            )
        )
    )
}

@Preview
@Composable
fun GradientTextLimitedPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            GradientTextLimited(
                text = "a text"
            )
        }
    }
}

@Preview
@Composable
fun GradientTextPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            GradientText(
                text = "a text",
                textSize = 15f
            )
        }
    }
}
