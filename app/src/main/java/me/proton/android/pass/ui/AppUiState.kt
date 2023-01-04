package me.proton.android.pass.ui

import androidx.compose.runtime.Immutable
import me.proton.android.pass.R
import me.proton.android.pass.network.api.NetworkStatus
import me.proton.android.pass.notifications.api.SnackbarMessage
import me.proton.android.pass.preferences.ThemePreference
import me.proton.pass.common.api.None
import me.proton.pass.common.api.Option
import me.proton.pass.presentation.navigation.drawer.DrawerUiState

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
