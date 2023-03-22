package proton.android.pass.featuresettings.impl

import androidx.annotation.StringRes
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.notifications.api.SnackbarType

enum class SettingsSnackbarMessage(
    @StringRes override val id: Int,
    override val type: SnackbarType,
    override val isClipboard: Boolean = false
) : SnackbarMessage {
    ErrorPerformingOperation(R.string.settings_error_performing_operation, SnackbarType.ERROR),
    ErrorPerformingSync(R.string.settings_force_sync_error, SnackbarType.ERROR),
    SyncSuccessful(R.string.settings_force_sync_success, SnackbarType.SUCCESS)
}
