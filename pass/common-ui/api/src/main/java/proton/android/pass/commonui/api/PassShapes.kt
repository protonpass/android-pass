package proton.android.pass.commonui.api

import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class PassShapes(
    val bottomsheetShape: Shape,
    val containerInputShape: Shape,
    val squircleShape: Shape
) {
    companion object {
        val Default: PassShapes = PassShapes(
            bottomsheetShape = RoundedCornerShape(
                topStart = Radius.medium,
                topEnd = Radius.medium,
                bottomStart = 0.dp,
                bottomEnd = 0.dp
            ),
            containerInputShape = RoundedCornerShape(Radius.small + Radius.extraSmall),
            squircleShape = RoundedCornerShape(Radius.medium)
        )
    }
}

val LocalPassShapes = staticCompositionLocalOf {
    PassShapes(
        bottomsheetShape = CutCornerShape(0.dp),
        containerInputShape = CutCornerShape(0.dp),
        squircleShape = CutCornerShape(0.dp),
    )
}

object Radius {
    val extraSmall: Dp = 4.dp
    val small: Dp = 8.dp
    val medium: Dp = 16.dp
    val large: Dp = 32.dp
    val extraLarge: Dp = 64.dp
}
