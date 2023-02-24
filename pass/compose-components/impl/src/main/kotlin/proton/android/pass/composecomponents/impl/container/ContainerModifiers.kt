package proton.android.pass.composecomponents.impl.container

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme

@Stable
fun Modifier.roundedContainer(borderColor: Color) = composed {
    clip(PassTheme.shapes.containerInputShape)
        .background(PassTheme.colors.inputBackground)
        .border(
            width = 1.dp,
            color = borderColor,
            shape = PassTheme.shapes.containerInputShape
        )
}
