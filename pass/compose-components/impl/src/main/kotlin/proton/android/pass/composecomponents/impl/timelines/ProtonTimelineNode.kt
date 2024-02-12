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

package proton.android.pass.composecomponents.impl.timelines

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.inset

data class ProtonTimelineNode(
    internal val id: String,
    internal val type: ProtonTimelineNodeType,
    internal val config: ProtonTimelineNodeConfig,
    internal val content: @Composable BoxScope.(modifier: Modifier) -> Unit,
) {

    @Composable
    internal fun Render() {
        Box(
            modifier = Modifier
                .wrapContentSize()
                .drawBehind {
                    val lineWidth = config.lineWidth.toPx()
                    val circleRadius = config.circleRadius.toPx()
                    val circleDiameter = circleRadius * 2
                    val verticalInset = size.height / 2 - circleDiameter

                    inset(vertical = verticalInset) {
                        drawCircle(
                            color = config.circleColor,
                            radius = circleRadius,
                            center = Offset(circleRadius, circleRadius),
                            style = when (type) {
                                ProtonTimelineNodeType.Child,
                                ProtonTimelineNodeType.Leaf -> Fill

                                ProtonTimelineNodeType.Root,
                                ProtonTimelineNodeType.Unique -> Stroke(width = lineWidth)
                            },
                        )

                        when (type) {
                            ProtonTimelineNodeType.Child,
                            ProtonTimelineNodeType.Root -> {
                                drawLine(
                                    brush = config.lineBrush,
                                    strokeWidth = config.lineWidth.toPx(),
                                    start = Offset(
                                        x = circleRadius,
                                        y = circleDiameter + lineWidth / 2,
                                    ),
                                    end = Offset(
                                        x = circleRadius,
                                        y = size.height + verticalInset * 2,
                                    ),
                                )
                            }

                            ProtonTimelineNodeType.Leaf,
                            ProtonTimelineNodeType.Unique -> {
                                // this node types do not required a line to be drawn
                            }

                        }
                    }
                },
        ) {
            content(
                Modifier.padding(
                    start = config.startOffset,
                    bottom = config.spaceBetweenNodes,
                ),
            )
        }
    }
}
