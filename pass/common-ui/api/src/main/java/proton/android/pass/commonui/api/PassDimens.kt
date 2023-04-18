package proton.android.pass.commonui.api

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class PassDimens(
    val bottomsheetHorizontalPadding: Dp,
    val bottomsheetVerticalPadding: Dp
) {
    companion object {
        val Phone: PassDimens = PassDimens(
            bottomsheetHorizontalPadding = Spacing.medium,
            bottomsheetVerticalPadding = Spacing.large
        )
    }
}

val LocalPassDimens = staticCompositionLocalOf {
    PassDimens(
        bottomsheetHorizontalPadding = 0.dp,
        bottomsheetVerticalPadding = 0.dp
    )
}

object Spacing {
    val extraSmall: Dp = 4.dp
    val small: Dp = 8.dp
    val medium: Dp = 16.dp
    val large: Dp = 32.dp
    val extraLarge: Dp = 64.dp
}
