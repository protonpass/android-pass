package proton.android.pass.featuresettings.impl

import androidx.compose.runtime.Stable
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.preferences.CopyTotpToClipboard
import proton.android.pass.preferences.ThemePreference

@Stable
data class SettingsUiState(
    val themePreference: ThemePreference,
    val copyTotpToClipboard: CopyTotpToClipboard,
    val isLoadingState: IsLoadingState,
    val appVersion: String
) {
    companion object {
        fun getInitialState(appVersion: String) = SettingsUiState(
            themePreference = ThemePreference.System,
            copyTotpToClipboard = CopyTotpToClipboard.NotEnabled,
            isLoadingState = IsLoadingState.NotLoading,
            appVersion = appVersion
        )
    }
}
