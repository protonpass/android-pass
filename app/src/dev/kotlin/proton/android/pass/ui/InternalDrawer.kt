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

package proton.android.pass.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.DrawerDefaults.scrimColor
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.contentColorFor
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import proton.android.pass.ui.internal.InternalDrawerState
import proton.android.pass.ui.internal.InternalDrawerValue
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun InternalDrawer(
    modifier: Modifier = Modifier,
    drawerState: InternalDrawerState,
    onOpenFeatureFlag: () -> Unit,
    onAppNavigation: (AppNavigation) -> Unit,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    BoxWithConstraints(modifier.fillMaxSize()) {
        if (!this@BoxWithConstraints.constraints.hasBoundedWidth) {
            error("Drawer shouldn't have infinite width")
        }
        val minValue = constraints.maxWidth.toFloat()
        val maxValue = 0f

        val anchors =
            mapOf(minValue to InternalDrawerValue.Closed, maxValue to InternalDrawerValue.Open)
        val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
        Box(
            modifier = Modifier.swipeable(
                state = drawerState,
                anchors = anchors,
                thresholds = { _, _ -> FractionalThreshold(FRACTION_THRESHOLD) },
                orientation = Orientation.Horizontal,
                enabled = true,
                reverseDirection = isRtl,
                resistance = null
            )
        ) {
            Box {
                content()
            }
            Scrim(
                open = drawerState.isOpen,
                onClose = {
                    if (drawerState.confirmStateChange(InternalDrawerValue.Closed)) {
                        scope.launch { drawerState.close() }
                    }
                },
                fraction = {
                    calculateFraction(minValue, maxValue, drawerState.offset.value)
                },
                color = scrimColor
            )
            Surface(
                modifier = with(LocalDensity.current) {
                    Modifier
                        .sizeIn(
                            minWidth = this@BoxWithConstraints.minWidth,
                            minHeight = this@BoxWithConstraints.minHeight,
                            maxWidth = this@BoxWithConstraints.maxWidth,
                            maxHeight = this@BoxWithConstraints.maxHeight
                        )
                }
                    .offset { IntOffset(drawerState.offset.value.roundToInt(), 0) },
                shape = CutCornerShape(0.dp),
                color = MaterialTheme.colors.surface,
                contentColor = contentColorFor(MaterialTheme.colors.surface),
                elevation = 16.dp
            ) {
                InternalDrawerContents(
                    onOpenFeatureFlag = onOpenFeatureFlag,
                    onAppNavigation = onAppNavigation
                )
            }
        }
    }
}

private const val FRACTION_THRESHOLD = 0.5f

@Composable
private fun Scrim(
    open: Boolean,
    onClose: () -> Unit,
    fraction: () -> Float,
    color: Color
) {
    val dismissDrawer = if (open) {
        Modifier.pointerInput(onClose) { detectTapGestures { onClose() } }
    } else {
        Modifier
    }

    Canvas(
        Modifier
            .fillMaxSize()
            .then(dismissDrawer)
    ) {
        drawRect(color, alpha = fraction())
    }
}

private fun calculateFraction(a: Float, b: Float, pos: Float) =
    ((pos - a) / (b - a)).coerceIn(0f, 1f)
