package proton.android.pass.ui

import androidx.annotation.StringRes
import proton.android.pass.R
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.notifications.api.SnackbarType

enum class AppSnackbarMessage(
    @StringRes override val id: Int,
    override val type: SnackbarType,
    override val isClipboard: Boolean = false
) : SnackbarMessage {
    CouldNotRefreshItems(R.string.snackbar_could_not_refresh_items, SnackbarType.ERROR),
    ErrorDuringStartup(R.string.snackbar_error_during_startup, SnackbarType.ERROR)
}

