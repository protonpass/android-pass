package me.proton.pass.presentation.settings

import me.proton.pass.presentation.uievents.IsButtonEnabled

data class SettingsUiState(
    val isFingerPrintEnabled: IsButtonEnabled
) {
    companion object {
        val Initial = SettingsUiState(
            isFingerPrintEnabled = IsButtonEnabled.Disabled
        )
    }
}
