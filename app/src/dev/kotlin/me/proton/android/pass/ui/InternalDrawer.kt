package me.proton.android.pass.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.Button
import androidx.compose.material.DrawerDefaults.scrimColor
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.airbnb.android.showkase.models.Showkase
import kotlinx.coroutines.launch
import me.proton.android.pass.ui.internal.InternalDrawerState
import me.proton.android.pass.ui.internal.InternalDrawerValue
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun InternalDrawer(
    modifier: Modifier = Modifier,
    drawerState: InternalDrawerState,
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
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colors.surface,
                contentColor = contentColorFor(MaterialTheme.colors.surface),
                elevation = 16.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {

                    Spacer(modifier = Modifier.height(10.dp))
                    val localContext = LocalContext.current
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            localContext.startActivity(Showkase.getBrowserIntent(localContext))
                        }
                    ) {
                        Text(text = "Showkase")
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
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
