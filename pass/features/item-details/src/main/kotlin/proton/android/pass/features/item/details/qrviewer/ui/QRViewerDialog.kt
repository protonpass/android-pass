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

package proton.android.pass.features.item.details.qrviewer.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.hilt.navigation.compose.hiltViewModel
import com.caverock.androidsvg.SVG
import proton.android.pass.composecomponents.impl.dialogs.NoPaddingDialog
import proton.android.pass.features.item.details.qrviewer.presentation.QRViewerViewModel
import proton.android.pass.features.item.details.shared.navigation.ItemDetailsNavDestination

@Composable
fun QRViewerDialog(onNavigated: (ItemDetailsNavDestination) -> Unit, viewModel: QRViewerViewModel = hiltViewModel()) {
    NoPaddingDialog(
        onDismissRequest = {
            onNavigated(ItemDetailsNavDestination.CloseScreen)
        }
    ) {
        var svg by remember { mutableStateOf<SVG?>(null) }

        LaunchedEffect(viewModel.rawSVG) {
            if (viewModel.rawSVG.isNotBlank()) {
                runCatching { SVG.getFromString(viewModel.rawSVG) }
                    .fold(
                        onSuccess = {
                            svg = it
                        },
                        onFailure = {
                            onNavigated(ItemDetailsNavDestination.CloseScreen)
                        }
                    )
            }
        }

        svg?.let { parsedSvg ->
            val aspectRatio = parsedSvg.documentWidth / parsedSvg.documentHeight

            Canvas(
                modifier = Modifier
                    .aspectRatio(aspectRatio, matchHeightConstraintsFirst = true)
            ) {
                drawIntoCanvas { canvas ->
                    val svgWidth = parsedSvg.documentWidth.toFloat()
                    val svgHeight = parsedSvg.documentHeight.toFloat()

                    val scaleX = size.width / svgWidth
                    val scaleY = size.height / svgHeight
                    val scale = minOf(scaleX, scaleY)

                    val offsetX = (size.width - svgWidth * scale) / 2
                    val offsetY = (size.height - svgHeight * scale) / 2

                    drawIntoCanvas { canvas ->
                        canvas.save()
                        canvas.translate(offsetX, offsetY)
                        canvas.scale(scale, scale)
                        parsedSvg.renderToCanvas(canvas.nativeCanvas)
                        canvas.restore()
                    }
                }
            }
        }
    }
}
