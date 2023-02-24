package proton.android.pass.commonui.api

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class PassColors(
    val backgroundNorm: Color,
    val backgroundStrongest: Color,
    val backgroundStrong: Color,
    val backgroundWeak: Color,

    val textNorm: Color,
    val textWeak: Color,
    val textHint: Color,
    val textDisabled: Color,
    val textInverted: Color,

    val accentBrandOpaque: Color,
    val accentBrandNorm: Color,
    val accentBrandWeak: Color,
    val accentBrandWeakest: Color,
    val accentBrandDark: Color,
    val accentPurpleOpaque: Color,
    val accentPurpleNorm: Color,
    val accentPurple40: Color,
    val accentPurpleWeak: Color,
    val accentPurpleWeakest: Color,
    val accentGreenOpaque: Color,
    val accentGreenNorm: Color,
    val accentGreenWeak: Color,
    val accentGreenWeakest: Color,
    val accentYellowOpaque: Color,
    val accentYellowNorm: Color,
    val accentYellowWeak: Color,
    val accentYellowWeakest: Color,
    val accentRedOpaque: Color,
    val accentRedNorm: Color,
    val accentRedWeak: Color,
    val accentRedWeakest: Color,

    val inputBackground: Color,
    val inputBorder: Color,
    val inputBorderFocused: Color
) {
    companion object {
        val Dark = PassColors(
            backgroundNorm = PassPalette.BackgroundNorm,
            backgroundStrongest = PassPalette.SearchBar,
            backgroundStrong = PassPalette.TabBar,
            backgroundWeak = PassPalette.Brand8,
            textNorm = PassPalette.White80,
            textWeak = PassPalette.White40,
            textHint = PassPalette.White24,
            textDisabled = PassPalette.White8,
            textInverted = PassPalette.BackgroundNorm,
            accentBrandOpaque = PassPalette.Brand100,
            accentBrandNorm = PassPalette.Brand80,
            accentBrandWeak = PassPalette.Brand24,
            accentBrandWeakest = PassPalette.Brand8,
            accentBrandDark = PassPalette.BrandDark,
            accentPurpleOpaque = PassPalette.Purple100,
            accentPurpleNorm = PassPalette.Purple80,
            accentPurple40 = PassPalette.Purple40,
            accentPurpleWeak = PassPalette.Purple24,
            accentPurpleWeakest = PassPalette.Purple8,
            accentGreenOpaque = PassPalette.Green100,
            accentGreenNorm = PassPalette.Green80,
            accentGreenWeak = PassPalette.Green24,
            accentGreenWeakest = PassPalette.Green8,
            accentYellowOpaque = PassPalette.Yellow100,
            accentYellowNorm = PassPalette.Yellow80,
            accentYellowWeak = PassPalette.Yellow24,
            accentYellowWeakest = PassPalette.Yellow8,
            accentRedOpaque = PassPalette.Red100,
            accentRedNorm = PassPalette.Red80,
            accentRedWeak = PassPalette.Red24,
            accentRedWeakest = PassPalette.Red8,
            inputBackground = PassPalette.White4,
            inputBorder = PassPalette.White8,
            inputBorderFocused = PassPalette.Purple80
        )
    }
}

val LocalPassColors = staticCompositionLocalOf {
    PassColors(
        backgroundNorm = Color.Unspecified,
        backgroundStrongest = Color.Unspecified,
        backgroundStrong = Color.Unspecified,
        backgroundWeak = Color.Unspecified,
        textNorm = Color.Unspecified,
        textWeak = Color.Unspecified,
        textHint = Color.Unspecified,
        textDisabled = Color.Unspecified,
        textInverted = Color.Unspecified,
        accentBrandOpaque = Color.Unspecified,
        accentBrandNorm = Color.Unspecified,
        accentBrandWeak = Color.Unspecified,
        accentBrandWeakest = Color.Unspecified,
        accentBrandDark = Color.Unspecified,
        accentPurpleOpaque = Color.Unspecified,
        accentPurpleNorm = Color.Unspecified,
        accentPurple40 = Color.Unspecified,
        accentPurpleWeak = Color.Unspecified,
        accentPurpleWeakest = Color.Unspecified,
        accentGreenOpaque = Color.Unspecified,
        accentGreenNorm = Color.Unspecified,
        accentGreenWeak = Color.Unspecified,
        accentGreenWeakest = Color.Unspecified,
        accentYellowOpaque = Color.Unspecified,
        accentYellowNorm = Color.Unspecified,
        accentYellowWeak = Color.Unspecified,
        accentYellowWeakest = Color.Unspecified,
        accentRedOpaque = Color.Unspecified,
        accentRedNorm = Color.Unspecified,
        accentRedWeak = Color.Unspecified,
        accentRedWeakest = Color.Unspecified,
        inputBackground = Color.Unspecified,
        inputBorder = Color.Unspecified,
        inputBorderFocused = Color.Unspecified
    )
}
