package proton.android.pass.featuresettings.impl

import androidx.compose.runtime.Stable
import proton.android.pass.autofill.api.AutofillStatus
import proton.android.pass.autofill.api.AutofillSupportedStatus
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.preferences.CopyTotpToClipboard
import proton.android.pass.preferences.ThemePreference

@Stable
data class SettingsUiState(
    val fingerprintSection: FingerprintSectionState,
    val themePreference: ThemePreference,
    val autofillStatus: AutofillSupportedStatus,
    val copyTotpToClipboard: CopyTotpToClipboard,
    val isLoadingState: IsLoadingState,
    val appVersion: String,
    val currentAccount: String
) {
    companion object {
        fun getInitialState(appVersion: String) = SettingsUiState(
            fingerprintSection = FingerprintSectionState.Available(IsButtonEnabled.Disabled),
            themePreference = ThemePreference.System,
            autofillStatus = AutofillSupportedStatus.Supported(AutofillStatus.Disabled),
            copyTotpToClipboard = CopyTotpToClipboard.NotEnabled,
            isLoadingState = IsLoadingState.NotLoading,
            appVersion = appVersion,
            currentAccount = ""
        )
    }
}

sealed interface FingerprintSectionState {
    data class Available(val enabled: IsButtonEnabled) : FingerprintSectionState
    object NoFingerprintRegistered : FingerprintSectionState
    object NotAvailable : FingerprintSectionState
}
