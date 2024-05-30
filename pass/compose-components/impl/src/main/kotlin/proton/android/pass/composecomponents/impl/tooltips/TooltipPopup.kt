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

import android.view.View
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import proton.android.pass.commonui.api.Radius
import proton.android.pass.commonui.api.Spacing
import kotlin.math.roundToInt

@Composable
fun TooltipPopup(
    modifier: Modifier = Modifier,
    requesterView: @Composable (Modifier) -> Unit,
    tooltipContent: @Composable () -> Unit,
    isVisible: Boolean
) {
    var isShowTooltip by remember { mutableStateOf(isVisible) }
    var position by remember { mutableStateOf(TooltipPopupPosition()) }

    val view = LocalView.current.rootView

    if (isShowTooltip) {
        TooltipPopup(
            onDismissRequest = {
                isShowTooltip = isShowTooltip.not()
            },
            position = position,
        ) {
            tooltipContent()
        }
    }
    requesterView(
        modifier
            .noRippleClickable {
                isShowTooltip = isShowTooltip.not()
            }
            .onGloballyPositioned { coordinates ->
                position = calculateTooltipPopupPosition(view, coordinates)
            }
    )
}

@Composable
fun TooltipPopup(
    position: TooltipPopupPosition,
    backgroundShape: Shape = RoundedCornerShape(size = Radius.small),
    arrowHeight: Dp = 8.dp,
    horizontalPadding: Dp = Spacing.large,
    onDismissRequest: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    var alignment = Alignment.TopCenter
    var offset = position.offset

    val horizontalPaddingInPx = with(LocalDensity.current) {
        horizontalPadding.toPx()
    }

    var arrowPositionX by remember { mutableFloatStateOf(position.centerPositionX) }

    with(LocalDensity.current) {
        val arrowPaddingPx = arrowHeight.toPx().roundToInt() * 3

        when (position.alignment) {
            TooltipAlignment.TopCenter -> {
                alignment = Alignment.TopCenter
                offset = offset.copy(
                    y = position.offset.y + arrowPaddingPx
                )
            }

            TooltipAlignment.BottomCenter -> {
                alignment = Alignment.BottomCenter
                offset = offset.copy(
                    y = position.offset.y - arrowPaddingPx
                )
            }
        }
    }

    val popupPositionProvider = remember(alignment, offset) {
        TooltipAlignmentOffsetPositionProvider(
            alignment = alignment,
            offset = offset,
            horizontalPaddingInPx = horizontalPaddingInPx,
            centerPositionX = position.centerPositionX,
        ) { position ->
            arrowPositionX = position
        }
    }

    Popup(
        popupPositionProvider = popupPositionProvider,
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(dismissOnBackPress = false),
    ) {
        BubbleLayout(
            modifier = Modifier
                .padding(horizontal = horizontalPadding),
//                .clip(shape = backgroundShape),
            alignment = position.alignment,
            arrowHeight = arrowHeight,
            arrowPositionX = arrowPositionX,
        ) {
            content()
        }
    }
}

internal class TooltipAlignmentOffsetPositionProvider(
    val alignment: Alignment,
    val offset: IntOffset,
    val centerPositionX: Float,
    val horizontalPaddingInPx: Float,
    private val onArrowPositionX: (Float) -> Unit,
) : PopupPositionProvider {

    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        var popupPosition = IntOffset(0, 0)

        // Get the aligned point inside the parent
        val parentAlignmentPoint = alignment.align(
            IntSize.Zero,
            IntSize(anchorBounds.width, anchorBounds.height),
            layoutDirection
        )
        // Get the aligned point inside the child
        val relativePopupPos = alignment.align(
            IntSize.Zero,
            IntSize(popupContentSize.width, popupContentSize.height),
            layoutDirection
        )

        // Add the position of the parent
        popupPosition += IntOffset(anchorBounds.left, anchorBounds.top)

        // Add the distance between the parent's top left corner and the alignment point
        popupPosition += parentAlignmentPoint

        // Subtract the distance between the children's top left corner and the alignment point
        popupPosition -= IntOffset(relativePopupPos.x, relativePopupPos.y)

        // Add the user offset
        val resolvedOffset = IntOffset(
            offset.x * if (layoutDirection == LayoutDirection.Ltr) 1 else -1,
            offset.y
        )

        popupPosition += resolvedOffset

        val leftSpace = centerPositionX - horizontalPaddingInPx
        val rightSpace = windowSize.width - centerPositionX - horizontalPaddingInPx

        val tooltipWidth = popupContentSize.width
        val halfPopupContentSize = popupContentSize.center.x

        val fullPadding = horizontalPaddingInPx * 2

        val maxTooltipSize = windowSize.width - fullPadding

        val isCentralPositionTooltip =
            halfPopupContentSize <= leftSpace && halfPopupContentSize <= rightSpace


        when {
            isCentralPositionTooltip -> {
                popupPosition =
                    IntOffset(centerPositionX.toInt() - halfPopupContentSize, popupPosition.y)
                val arrowPosition = halfPopupContentSize.toFloat() - horizontalPaddingInPx
                onArrowPositionX.invoke(arrowPosition)
            }

            tooltipWidth >= maxTooltipSize -> {
                popupPosition =
                    IntOffset(windowSize.center.x - halfPopupContentSize, popupPosition.y)
                val arrowPosition = popupPosition.x.toFloat() // start
//                val arrowPosition = popupPosition.x + maxTooltipSize // end
//                val arrowPosition = (popupPosition.x + maxTooltipSize) / 2 // center
                onArrowPositionX.invoke(arrowPosition)
            }

            halfPopupContentSize > rightSpace -> {
                popupPosition = IntOffset(centerPositionX.toInt(), popupPosition.y)
                val arrowPosition =
                    halfPopupContentSize + (halfPopupContentSize - rightSpace) - fullPadding

                onArrowPositionX.invoke(arrowPosition)
            }

            halfPopupContentSize > leftSpace -> {
                popupPosition = IntOffset(0, popupPosition.y)
                val arrowPosition = centerPositionX - horizontalPaddingInPx
                onArrowPositionX.invoke(arrowPosition)
            }

            else -> {
                val position = centerPositionX
                onArrowPositionX.invoke(position)
            }
        }

        return popupPosition
    }
}

@Composable
fun BubbleLayout(
    modifier: Modifier = Modifier,
    alignment: TooltipAlignment = TooltipAlignment.TopCenter,
    arrowHeight: Dp,
    arrowPositionX: Float,
    content: @Composable () -> Unit
) {

    val arrowHeightPx = with(LocalDensity.current) {
        arrowHeight.toPx()
    }

    Box(
        modifier = modifier
            .drawBehind {
                println("JIBIRI: arrowPositionX $arrowPositionX")
//                if (arrowPositionX <= 0f) return@drawBehind

                val isTopCenter = alignment == TooltipAlignment.TopCenter

                val path = Path()

                if (isTopCenter) {
                    println("JIBIRI: isTopCenter")
                    val position = Offset(arrowPositionX.plus(arrowHeightPx), 0f)
                    path.apply {
                        moveTo(x = position.x, y = position.y)
                        lineTo(x = position.x - arrowHeightPx, y = position.y)
                        lineTo(x = position.x, y = position.y - arrowHeightPx)
                        lineTo(x = position.x + arrowHeightPx, y = position.y)
                        lineTo(x = position.x, y = position.y)
                    }
                } else {
                    val arrowY = drawContext.size.height
                    val position = Offset(arrowPositionX, arrowY)
                    path.apply {
                        moveTo(x = position.x, y = position.y)
                        lineTo(x = position.x + arrowHeightPx, y = position.y)
                        lineTo(x = position.x, y = position.y + arrowHeightPx)
                        lineTo(x = position.x - arrowHeightPx, y = position.y)
                        lineTo(x = position.x, y = position.y)
                    }
                }

                drawPath(
                    path = path,
                    color = Color.Red,
                )
                path.close()
            }
    ) {
        content()
    }
}

data class TooltipPopupPosition(
    val offset: IntOffset = IntOffset(0, 0),
    val alignment: TooltipAlignment = TooltipAlignment.TopCenter,

    val centerPositionX: Float = 0f,
)

fun calculateTooltipPopupPosition(
    view: View,
    coordinates: LayoutCoordinates?,
): TooltipPopupPosition {
    coordinates ?: return TooltipPopupPosition()

    val visibleWindowBounds = android.graphics.Rect()
    view.getWindowVisibleDisplayFrame(visibleWindowBounds)

    val boundsInWindow = coordinates.boundsInWindow()

    val heightAbove = boundsInWindow.top - visibleWindowBounds.top
    val heightBelow = visibleWindowBounds.bottom - visibleWindowBounds.top - boundsInWindow.bottom

    val centerPositionX = boundsInWindow.right - (boundsInWindow.right - boundsInWindow.left) / 2

    val offsetX = centerPositionX - visibleWindowBounds.centerX()

    return if (heightAbove < heightBelow) {
        val offset = IntOffset(
            y = coordinates.size.height,
            x = offsetX.toInt()
        )
        TooltipPopupPosition(
            offset = offset,
            alignment = TooltipAlignment.TopCenter,
            centerPositionX = centerPositionX,
        )
    } else {
        TooltipPopupPosition(
            offset = IntOffset(
                y = -coordinates.size.height,
                x = offsetX.toInt()
            ),
            alignment = TooltipAlignment.BottomCenter,
            centerPositionX = centerPositionX,
        )
    }
}

enum class TooltipAlignment {
    BottomCenter,
    TopCenter,
}

fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() }) {
        onClick()
    }
}
