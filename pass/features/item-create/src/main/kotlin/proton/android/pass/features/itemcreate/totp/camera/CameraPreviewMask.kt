/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.features.itemcreate.totp.camera

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

private val OverlayCornerRadiusSmall = 4.dp
private val OverlayCornerRadiusLarge = 8.dp
private val OverlayInsetPadding = 4.dp
private val StrokeWidth = 6.dp
private const val INSET_OFFSET_FACTOR = 2
private const val INSET_SIZE_FACTOR = 4

@Composable
internal fun CameraPreviewMask(cutoutRect: Rect, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val insetPx = OverlayInsetPadding.toPx()
        val smallRadiusPx = OverlayCornerRadiusSmall.toPx()
        val largeRadiusPx = OverlayCornerRadiusLarge.toPx()

        // Dark overlay with transparent cutout hole
        Path().apply {
            fillType = PathFillType.EvenOdd
            addRect(Rect(offset = Offset.Zero, size = Size(size.width, size.height)))
            addRoundRect(
                RoundRect(
                    rect = Rect(
                        offset = Offset(
                            x = cutoutRect.left + insetPx * INSET_OFFSET_FACTOR,
                            y = cutoutRect.top + insetPx * INSET_OFFSET_FACTOR
                        ),
                        size = Size(
                            width = cutoutRect.width - insetPx * INSET_SIZE_FACTOR,
                            height = cutoutRect.height - insetPx * INSET_SIZE_FACTOR
                        )
                    ),
                    cornerRadius = CornerRadius(smallRadiusPx)
                )
            )
        }.also { path ->
            drawPath(path = path, color = Color.Black.copy(alpha = 0.6f))
        }

        // Corner bracket strokes drawn as a dashed rounded rect
        Path().apply {
            addRoundRect(
                RoundRect(
                    rect = Rect(
                        offset = Offset(cutoutRect.left, cutoutRect.top),
                        size = Size(cutoutRect.width, cutoutRect.height)
                    ),
                    cornerRadius = CornerRadius(largeRadiusPx)
                )
            )
        }.also { path ->
            val lineInterval = cutoutRect.width / 2f
            val gapInterval = cutoutRect.width / 2f - smallRadiusPx
            val phase = cutoutRect.width / 4f + smallRadiusPx * 1.5f

            drawPath(
                path = path,
                color = Color.White,
                style = Stroke(
                    width = StrokeWidth.toPx(),
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(
                        intervals = floatArrayOf(lineInterval, gapInterval),
                        phase = phase
                    )
                )
            )
        }
    }
}
