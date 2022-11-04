package me.proton.pass.presentation.settings

import me.proton.android.pass.preferences.ThemePreference
import me.proton.pass.presentation.uievents.IsButtonEnabled

data class SettingsUiState(
    val fingerprintSection: FingerprintSectionState,
    val themePreference: ThemePreference
) {
    companion object {
        val Initial = SettingsUiState(
            fingerprintSection = FingerprintSectionState.Available(IsButtonEnabled.Disabled),
            themePreference = ThemePreference.System
        )
    }
}

sealed interface FingerprintSectionState {
    data class Available(val enabled: IsButtonEnabled) : FingerprintSectionState
    object NoFingerprintRegistered : FingerprintSectionState
    object NotAvailable : FingerprintSectionState
}
