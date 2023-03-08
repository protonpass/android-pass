package proton.android.pass.ui

import androidx.compose.runtime.Immutable
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.network.api.NetworkStatus
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.preferences.ThemePreference

@Immutable
data class AppUiState(
    val snackbarMessage: Option<SnackbarMessage>,
    val theme: ThemePreference,
    val networkStatus: NetworkStatus
) {
    companion object {
        fun Initial(theme: ThemePreference) = AppUiState(
            snackbarMessage = None,
            theme = theme,
            networkStatus = NetworkStatus.Online
        )
    }
}
