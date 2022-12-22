package me.proton.pass.presentation.settings

import androidx.compose.runtime.Stable
import me.proton.android.pass.autofill.api.AutofillStatus
import me.proton.android.pass.autofill.api.AutofillSupportedStatus
import me.proton.android.pass.preferences.ThemePreference
import me.proton.pass.presentation.uievents.IsButtonEnabled
import me.proton.pass.presentation.uievents.IsLoadingState

@Stable
data class SettingsUiState(
    val fingerprintSection: FingerprintSectionState,
    val themePreference: ThemePreference,
    val autofillStatus: AutofillSupportedStatus,
    val isLoadingState: IsLoadingState,
    val appVersion: String
) {
    companion object {
        fun getInitialState(appVersion: String) = SettingsUiState(
            fingerprintSection = FingerprintSectionState.Available(IsButtonEnabled.Disabled),
            themePreference = ThemePreference.System,
            autofillStatus = AutofillSupportedStatus.Supported(AutofillStatus.Disabled),
            isLoadingState = IsLoadingState.NotLoading,
            appVersion = appVersion
        )
    }
}

sealed interface FingerprintSectionState {
    data class Available(val enabled: IsButtonEnabled) : FingerprintSectionState
    object NoFingerprintRegistered : FingerprintSectionState
    object NotAvailable : FingerprintSectionState
}
