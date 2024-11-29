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

package proton.android.pass.composecomponents.impl.buttons

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ButtonElevation
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import proton.android.pass.composecomponents.impl.icon.Icon

object Button {
    @Composable
    fun Circular(
        modifier: Modifier = Modifier,
        color: Color,
        borderStroke: BorderStroke? = null,
        contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
        elevation: ButtonElevation = ButtonDefaults.elevation(),
        enabled: Boolean = true,
        onClick: () -> Unit,
        content: @Composable RowScope.() -> Unit
    ) {
        Button(
            modifier = modifier,
            contentPadding = contentPadding,
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = color,
                disabledBackgroundColor = color.copy(alpha = 0.3f)
            ),
            shape = CircleShape,
            border = borderStroke,
            elevation = elevation,
            onClick = onClick
        ) {
            content()
        }
    }

    @Composable
    fun CircleIcon(
        modifier: Modifier = Modifier,
        backgroundColor: Color,
        size: Dp = Dp.Unspecified,
        enabled: Boolean = true,
        @DrawableRes iconId: Int,
        iconTint: Color,
        onClick: () -> Unit
    ) {
        val adjustedBackgroundColor =
            if (enabled) backgroundColor else backgroundColor.copy(alpha = 0.3f)
        val adjustedIconTint = if (enabled) iconTint else iconTint.copy(alpha = 0.3f)
        IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier
                .clip(CircleShape)
                .size(size)
                .background(adjustedBackgroundColor)
        ) {
            Icon.Default(
                id = iconId,
                tint = adjustedIconTint
            )
        }
    }
}
