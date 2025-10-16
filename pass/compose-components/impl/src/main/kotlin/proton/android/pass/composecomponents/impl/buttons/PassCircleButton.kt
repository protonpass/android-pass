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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.applyIf

@Composable
fun PassCircleButton(
    modifier: Modifier = Modifier,
    fillMaxWidth: Boolean = true,
    onClick: () -> Unit,
    text: String,
    backgroundColor: Color = PassTheme.colors.interactionNormMajor2,
    textColor: Color = PassTheme.colors.textInvert,
    isLoading: Boolean = false,
    contentHorizontalPadding: Dp = 0.dp,
    contentVerticalPadding: Dp = Spacing.medium
) {
    Box(
        modifier = modifier
            .applyIf(
                condition = fillMaxWidth,
                ifTrue = {
                    Modifier.fillMaxWidth()
                }
            )
            .clip(CircleShape)
            .background(color = backgroundColor)
            .applyIf(
                condition = !isLoading,
                ifTrue = { clickable(onClick = onClick) }
            )
            .padding(horizontal = contentHorizontalPadding, vertical = contentVerticalPadding)
    ) {
        Row(
            modifier = Modifier.align(Alignment.Center),
            horizontalArrangement = Arrangement.spacedBy(space = Spacing.medium)
        ) {
            Text(
                text = text,
                color = textColor,
                style = ProtonTheme.typography.defaultNorm,
                textAlign = TextAlign.Center
            )

            AnimatedVisibility(visible = isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(size = 20.dp)
                        .align(alignment = Alignment.CenterVertically)
                )
            }
        }
    }
}
