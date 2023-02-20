package proton.android.pass.composecomponents.impl.container

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp

private const val BACKGROUND_ALPHA = 0.7f

@Stable
fun Modifier.roundedContainer(borderColor: Color) =
    clip(RoundedCornerShape(12.dp))
        .background(lerp(Color.White, Color.Transparent, BACKGROUND_ALPHA))
        .border(
            width = 1.dp,
            color = borderColor,
            shape = RoundedCornerShape(12.dp)
        )
