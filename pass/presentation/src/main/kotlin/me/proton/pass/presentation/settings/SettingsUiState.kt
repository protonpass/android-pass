package me.proton.pass.presentation.settings

import me.proton.android.pass.autofill.api.AutofillStatus
import me.proton.android.pass.autofill.api.AutofillSupportedStatus
import me.proton.android.pass.preferences.ThemePreference
import me.proton.pass.presentation.uievents.IsButtonEnabled

data class SettingsUiState(
    val fingerprintSection: FingerprintSectionState,
    val themePreference: ThemePreference,
    val autofillStatus: AutofillSupportedStatus
) {
    companion object {
        val Initial = SettingsUiState(
            fingerprintSection = FingerprintSectionState.Available(IsButtonEnabled.Disabled),
            themePreference = ThemePreference.System,
            autofillStatus = AutofillSupportedStatus.Supported(AutofillStatus.Disabled)
        )
    }
}

sealed interface FingerprintSectionState {
    data class Available(val enabled: IsButtonEnabled) : FingerprintSectionState
    object NoFingerprintRegistered : FingerprintSectionState
    object NotAvailable : FingerprintSectionState
}
