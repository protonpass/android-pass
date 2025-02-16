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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.proton.core.presentation.R
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.applyIf

@Composable
fun CircleIconButton(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    size: Dp = Dp.Unspecified,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    IconButton(
        modifier = modifier
            .clip(CircleShape)
            .size(size)
            .background(backgroundColor),
        onClick = onClick
    ) { content() }
}

@Composable
fun CircleIconButton(
    @DrawableRes drawableRes: Int,
    backgroundColor: Color,
    tintColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Int? = null,
    enabled: Boolean = true,
    onDisabledClick: (() -> Unit)? = null,
    iconContentDescription: String? = null
) {
    IconButton(
        modifier = modifier
            .clip(CircleShape)
            .applyIf(
                condition = size != null,
                ifTrue = { size(size!!.dp) }
            )
            .background(
                color = if (enabled) backgroundColor else backgroundColor.copy(alpha = 0.6f)
            ),
        enabled = enabled || onDisabledClick != null,
        onClick = { if (enabled) onClick() else onDisabledClick?.invoke() }
    ) {
        Icon(
            painter = painterResource(drawableRes),
            contentDescription = iconContentDescription,
            tint = if (enabled) tintColor else tintColor.copy(alpha = 0.2f)
        )
    }
}

@Preview
@Composable
fun CircleIconButtonPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            CircleIconButton(
                drawableRes = R.drawable.ic_proton_arrows_rotate,
                backgroundColor = PassTheme.colors.aliasInteractionNormMajor1,
                tintColor = PassTheme.colors.loginInteractionNormMajor1,
                onClick = {}
            )
        }
    }
}
