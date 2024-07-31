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

package proton.android.pass.composecomponents.impl.tooltips

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing

@Composable
fun PassTooltipPopup(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    position: MutableState<IntOffset>,
    size: MutableState<IntSize>,
    backgroundColor: Color = PassTheme.colors.searchBarBackground,
    onDismissRequest: () -> Unit
) {
    BackHandler { onDismissRequest() }
    Popup(
        popupPositionProvider = remember(position.value, size.value) {
            object : PopupPositionProvider {
                override fun calculatePosition(
                    anchorBounds: IntRect,
                    windowSize: IntSize,
                    layoutDirection: LayoutDirection,
                    popupContentSize: IntSize
                ): IntOffset = IntOffset(
                    x = 0,
                    y = position.value.y + size.value.height
                )
            }
        },
        properties = PopupProperties(),
        onDismissRequest = onDismissRequest
    ) {
        val density = LocalDensity.current
        val itemCenter = with(density) { (position.value.x + size.value.width).toDp() }

        PassTooltip(
            modifier = Modifier.padding(horizontal = Spacing.large),
            title = title,
            description = description,
            backgroundColor = backgroundColor,
            arrowOffset = (itemCenter - 2 * Spacing.large).coerceAtLeast(0.dp),
            onClose = onDismissRequest
        )
    }
}
