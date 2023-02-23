package proton.android.pass.commonui.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import me.proton.core.compose.theme.ProtonColors
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.isNightMode

@Composable
fun PassTheme(
    isDark: Boolean = isNightMode(),
    protonColors: ProtonColors = if (isDark) ProtonColors.Dark else ProtonColors.Light,
    passColors: PassColors = if (isDark) PassColors.Dark else PassColors.Dark,
    passDimens: PassDimens = PassDimens.Phone,
    passShapes: PassShapes = PassShapes.Default,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalPassColors provides passColors,
        LocalPassDimens provides passDimens,
        LocalPassShapes provides passShapes
    ) {
        ProtonTheme(
            isDark = isDark,
            colors = protonColors,
            content = content
        )
    }
}

object PassTheme {
    val colors: PassColors
        @Composable
        get() = LocalPassColors.current
    val dimens: PassDimens
        @Composable
        get() = LocalPassDimens.current
    val shapes: PassShapes
        @Composable
        get() = LocalPassShapes.current
}
