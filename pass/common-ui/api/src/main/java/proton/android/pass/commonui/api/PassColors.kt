package proton.android.pass.commonui.api

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class PassColors(

    val interactionNormMajor1: Color,
    val interactionNorm: Color,
    val interactionNormMinor1: Color,
    val interactionNormMinor2: Color,

    val loginInteractionNormMajor1: Color,
    val loginInteractionNorm: Color,
    val loginInteractionNormMinor1: Color,
    val loginInteractionNormMinor2: Color,

    val aliasInteractionNormMajor1: Color,
    val aliasInteractionNorm: Color,
    val aliasInteractionNormMinor1: Color,
    val aliasInteractionNormMinor2: Color,

    val noteInteractionNormMajor1: Color,
    val noteInteractionNorm: Color,
    val noteInteractionNormMinor1: Color,
    val noteInteractionNormMinor2: Color,

    val passwordInteractionNormMajor1: Color,
    val passwordInteractionNorm: Color,
    val passwordInteractionNormMinor1: Color,
    val passwordInteractionNormMinor2: Color,

    val textNorm: Color,
    val textWeak: Color,
    val textHint: Color,
    val textDisabled: Color,
    val textInvert: Color,

    val inputBackground: Color,
    val inputBorder: Color,
    val inputBorderFocused: Color,

    val backgroundNorm: Color,
    val backgroundWeak: Color,
    val backgroundStrong: Color,
    val backgroundStrongest: Color,

    val signalDanger: Color,
    val signalWarning: Color,
    val signalSuccess: Color,
    val signalInfo: Color,
    val signalNorm: Color,
) {
    companion object {
        val Dark = PassColors(
            interactionNormMajor1 = PassPalette.VeryLightBlue100,
            interactionNorm = PassPalette.VeryLightBlue80,
            interactionNormMinor1 = PassPalette.VeryLightBlue16,
            interactionNormMinor2 = PassPalette.VeryLightBlue8,
            loginInteractionNormMajor1 = PassPalette.Lavender100,
            loginInteractionNorm = PassPalette.Lavender80,
            loginInteractionNormMinor1 = PassPalette.Lavender16,
            loginInteractionNormMinor2 = PassPalette.Lavender8,
            aliasInteractionNormMajor1 = PassPalette.GreenSheen100,
            aliasInteractionNorm = PassPalette.GreenSheen80,
            aliasInteractionNormMinor1 = PassPalette.GreenSheen16,
            aliasInteractionNormMinor2 = PassPalette.GreenSheen8,
            noteInteractionNormMajor1 = PassPalette.MacaroniAndCheese100,
            noteInteractionNorm = PassPalette.MacaroniAndCheese80,
            noteInteractionNormMinor1 = PassPalette.MacaroniAndCheese16,
            noteInteractionNormMinor2 = PassPalette.MacaroniAndCheese8,
            passwordInteractionNormMajor1 = PassPalette.VenetianRed100,
            passwordInteractionNorm = PassPalette.VenetianRed80,
            passwordInteractionNormMinor1 = PassPalette.VenetianRed16,
            passwordInteractionNormMinor2 = PassPalette.VenetianRed8,
            textNorm = PassPalette.White80,
            textWeak = PassPalette.White40,
            textHint = PassPalette.White24,
            textDisabled = PassPalette.White8,
            textInvert = PassPalette.EerieBlack,
            inputBackground = PassPalette.Cultured,
            inputBorder = PassPalette.BrightGray,
            inputBorderFocused = PassPalette.Lavender8,
            backgroundNorm = PassPalette.EerieBlack,
            backgroundWeak = PassPalette.DarkGunmetal,
            backgroundStrong = PassPalette.ChineseBlack80,
            backgroundStrongest = PassPalette.SmokyBlack,
            signalDanger = PassPalette.VanillaIce,
            signalWarning = PassPalette.PastelOrange,
            signalSuccess = PassPalette.OceanGreen,
            signalInfo = PassPalette.PictonBlue,
            signalNorm = PassPalette.White100
        )
    }
}

val LocalPassColors = staticCompositionLocalOf {
    PassColors(
        interactionNormMajor1 = Color.Unspecified,
        interactionNorm = Color.Unspecified,
        interactionNormMinor1 = Color.Unspecified,
        interactionNormMinor2 = Color.Unspecified,
        loginInteractionNormMajor1 = Color.Unspecified,
        loginInteractionNorm = Color.Unspecified,
        loginInteractionNormMinor1 = Color.Unspecified,
        loginInteractionNormMinor2 = Color.Unspecified,
        aliasInteractionNormMajor1 = Color.Unspecified,
        aliasInteractionNorm = Color.Unspecified,
        aliasInteractionNormMinor1 = Color.Unspecified,
        aliasInteractionNormMinor2 = Color.Unspecified,
        noteInteractionNormMajor1 = Color.Unspecified,
        noteInteractionNorm = Color.Unspecified,
        noteInteractionNormMinor1 = Color.Unspecified,
        noteInteractionNormMinor2 = Color.Unspecified,
        passwordInteractionNormMajor1 = Color.Unspecified,
        passwordInteractionNorm = Color.Unspecified,
        passwordInteractionNormMinor1 = Color.Unspecified,
        passwordInteractionNormMinor2 = Color.Unspecified,
        textNorm = Color.Unspecified,
        textWeak = Color.Unspecified,
        textHint = Color.Unspecified,
        textDisabled = Color.Unspecified,
        textInvert = Color.Unspecified,
        inputBackground = Color.Unspecified,
        inputBorder = Color.Unspecified,
        inputBorderFocused = Color.Unspecified,
        backgroundNorm = Color.Unspecified,
        backgroundWeak = Color.Unspecified,
        backgroundStrong = Color.Unspecified,
        backgroundStrongest = Color.Unspecified,
        signalDanger = Color.Unspecified,
        signalWarning = Color.Unspecified,
        signalSuccess = Color.Unspecified,
        signalInfo = Color.Unspecified,
        signalNorm = Color.Unspecified
    )
}
