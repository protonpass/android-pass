package proton.android.pass.composecomponents.impl.modifiers

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import proton.android.pass.commonui.api.applyIf

@Composable
fun Modifier.compatOverlayBlur(fallbackColor: Color, blurRadius: Dp): Modifier = this.applyIf(
    condition = Build.VERSION.SDK_INT >= 32,
    ifTrue = { Modifier.blur(blurRadius) },
    ifFalse = { Modifier.placeholder(fallbackColor) }
)
