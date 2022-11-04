package me.proton.pass.presentation.settings

import me.proton.android.pass.preferences.ThemePreference
import me.proton.pass.presentation.uievents.IsButtonEnabled

data class SettingsUiState(
    val isFingerPrintEnabled: IsButtonEnabled,
    val themePreference: ThemePreference
) {
    companion object {
        val Initial = SettingsUiState(
            isFingerPrintEnabled = IsButtonEnabled.Disabled,
            themePreference = ThemePreference.System
        )
    }
}
