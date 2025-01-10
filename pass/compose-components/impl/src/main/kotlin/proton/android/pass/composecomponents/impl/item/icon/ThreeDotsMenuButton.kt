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

package proton.android.pass.composecomponents.impl.item.icon

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.applyIfNotNull
import proton.android.pass.composecomponents.impl.R
import me.proton.core.presentation.R as CoreR

@Composable
fun ThreeDotsMenuButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    dotsColor: Color = if (enabled) {
        PassTheme.colors.textWeak
    } else {
        PassTheme.colors.textDisabled
    },
    backgroundColor: Color? = null,
    size: Dp? = null,
    contentDescription: String? = null,
    onClick: () -> Unit
) {
    IconButton(
        modifier = modifier
            .clip(CircleShape)
            .applyIfNotNull(
                value = size,
                ifNotNull = { size(it) }
            )
            .applyIfNotNull(
                value = backgroundColor,
                ifNotNull = { color ->
                    if (enabled) {
                        background(color)
                    } else {
                        background(color.copy(alpha = 0.6f))
                    }
                }
            ),
        enabled = enabled,
        onClick = onClick
    ) {
        val resource = if (backgroundColor == null) {
            R.drawable.ic_three_dots_vertical_24
        } else {
            CoreR.drawable.ic_proton_three_dots_vertical
        }
        Icon(
            painter = painterResource(resource),
            contentDescription = contentDescription
                ?: stringResource(R.string.options_menu_content_description),
            tint = dotsColor
        )
    }
}

@Preview
@Composable
internal fun ThreeDotsMenuButtonPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            ThreeDotsMenuButton(
                onClick = {}
            )
        }
    }
}
