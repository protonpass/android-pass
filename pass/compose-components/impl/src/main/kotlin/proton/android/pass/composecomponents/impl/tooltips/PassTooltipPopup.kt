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

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import proton.android.pass.commonui.api.Spacing
import kotlin.math.roundToInt

@Composable
fun PassTooltipPopup(
    modifier: Modifier = Modifier,
    @StringRes titleResId: Int,
    @StringRes descriptionResId: Int,
    requesterView: @Composable () -> Unit,
    onClose: () -> Unit,
    shouldDisplayTooltip: Boolean,
    arrowHeight: Dp,
    backgroundColor: Color,
    horizontalPadding: Dp = Spacing.large,
    verticalPadding: Dp = Spacing.large - Spacing.small
) {
    var isTooltipVisible by remember { mutableStateOf(shouldDisplayTooltip) }

    requesterView()

    if (isTooltipVisible) {
        var arrowPositionX by remember { mutableFloatStateOf(0f) }

        val initialOffset = with(LocalDensity.current) {
            IntOffset(
                x = 0,
                y = arrowHeight.toPx().roundToInt() * 3
            )
        }

        val popupPositionProvider = remember {
            PassTooltipPopupPositionProvider(initialOffset) { newArrowPositionX ->
                arrowPositionX = newArrowPositionX
            }
        }

        Popup(
            popupPositionProvider = popupPositionProvider,
            properties = PopupProperties(dismissOnBackPress = false),
            onDismissRequest = { isTooltipVisible = false }
        ) {
            val arrowHeightPx = with(LocalDensity.current) { arrowHeight.toPx() }

            Box(
                modifier = modifier
                    .padding(
                        horizontal = horizontalPadding,
                        vertical = verticalPadding
                    )
                    .drawBehind {
                        val position = Offset(
                            x = arrowPositionX.plus(arrowHeightPx),
                            y = 0f
                        )

                        Path()
                            .apply {
                                moveTo(x = position.x, y = position.y)
                                lineTo(x = position.x - arrowHeightPx, y = position.y)
                                lineTo(x = position.x, y = position.y - arrowHeightPx)
                                lineTo(x = position.x + arrowHeightPx, y = position.y)
                                lineTo(x = position.x, y = position.y)
                                close()
                            }
                            .also { trianglePath ->
                                drawPath(
                                    path = trianglePath,
                                    color = backgroundColor
                                )
                            }
                    }
            ) {
                PassTooltip(
                    title = stringResource(id = titleResId),
                    description = stringResource(id = descriptionResId),
                    backgroundColor = backgroundColor,
                    onClose = {
                        isTooltipVisible = false
                        onClose()
                    }
                )
            }
        }
    }
}
