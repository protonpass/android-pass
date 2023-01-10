package proton.android.pass.ui

import androidx.compose.runtime.Immutable
import proton.android.pass.R
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.network.api.NetworkStatus
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.preferences.ThemePreference
import proton.android.pass.presentation.navigation.drawer.DrawerUiState

@Immutable
data class AppUiState(
    val snackbarMessage: Option<SnackbarMessage>,
    val drawerUiState: DrawerUiState,
    val theme: ThemePreference,
    val networkStatus: NetworkStatus
) {
    companion object {
        fun Initial(theme: ThemePreference) = AppUiState(
            snackbarMessage = None,
            drawerUiState = DrawerUiState(
                appNameResId = R.string.app_name
            ),
            theme = theme,
            networkStatus = NetworkStatus.Online
        )
    }
}
