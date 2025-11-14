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

package proton.android.pass.composecomponents.impl.container

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.applyIf

@Composable
fun Pill(
    modifier: Modifier = Modifier,
    minSize: Dp = 40.dp,
    backgroundColor: Color,
    showClickEffect: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .defaultMinSize(minSize, minSize)
            .height(minSize)
            .clip(RoundedCornerShape(percent = 50))
            .applyIf(
                condition = onClick != null,
                ifTrue = {
                    val indication = if (showClickEffect) LocalIndication.current else null
                    clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = indication,
                        onClick = { onClick?.invoke() }
                    )
                }
            )
            .background(backgroundColor)
            .padding(horizontal = Spacing.extraSmall),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
