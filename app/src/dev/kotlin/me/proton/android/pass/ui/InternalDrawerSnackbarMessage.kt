package me.proton.android.pass.ui

import androidx.annotation.StringRes
import me.proton.android.pass.R
import me.proton.android.pass.notifications.api.SnackbarMessage
import me.proton.android.pass.notifications.api.SnackbarType

enum class InternalDrawerSnackbarMessage(
    @StringRes override val id: Int,
    override val type: SnackbarType,
    override val isClipboard: Boolean = false
): SnackbarMessage {
    PreferencesCleared(R.string.snackbar_preferences_cleared_success, SnackbarType.SUCCESS),
    PreferencesClearError(R.string.snackbar_preferences_cleared_error, SnackbarType.ERROR)
}
