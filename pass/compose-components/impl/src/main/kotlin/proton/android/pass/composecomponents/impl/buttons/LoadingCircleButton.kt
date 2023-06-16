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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.applyIf

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LoadingCircleButton(
    modifier: Modifier = Modifier,
    text: @Composable RowScope.() -> Unit,
    leadingIcon: (@Composable () -> Unit)? = null,
    color: Color,
    isLoading: Boolean,
    buttonEnabled: Boolean = true,
    buttonHeight: Dp = 20.dp,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .clip(CircleShape)
            .applyIf(condition = !isLoading && buttonEnabled, ifTrue = { clickable { onClick() } })
            .background(color),
        horizontalArrangement = Arrangement.Center
    ) {
        AnimatedContent(modifier = Modifier.padding(16.dp, 10.dp), targetState = isLoading) {
            if (it) {
                CircularProgressIndicator(
                    modifier = Modifier.size(buttonHeight),
                    strokeWidth = 2.dp,
                    color = ProtonTheme.colors.iconInverted
                )
            } else {
                Row(
                    modifier = Modifier.height(buttonHeight),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    leadingIcon?.invoke()
                    text()
                }
            }
        }
    }
}
