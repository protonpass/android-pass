package proton.android.pass.ui

import androidx.annotation.StringRes
import proton.android.pass.R
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.notifications.api.SnackbarType

enum class InternalDrawerSnackbarMessage(
    @StringRes override val id: Int,
    override val type: SnackbarType,
    override val isClipboard: Boolean = false
): SnackbarMessage {
    PreferencesCleared(R.string.snackbar_preferences_cleared_success, SnackbarType.SUCCESS),
    PreferencesClearError(R.string.snackbar_preferences_cleared_error, SnackbarType.ERROR),
    EmptyShareError(R.string.snackbar_empty_share, SnackbarType.ERROR),
    DeleteVaultSuccess(R.string.snackbar_delete_vault_success, SnackbarType.SUCCESS),
    DeleteVaultError(R.string.snackbar_delete_vault_error, SnackbarType.ERROR)
}
