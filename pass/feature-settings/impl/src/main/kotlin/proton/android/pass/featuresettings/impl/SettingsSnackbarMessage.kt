package proton.android.pass.featuresettings.impl

import androidx.annotation.StringRes
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.notifications.api.SnackbarType

enum class SettingsSnackbarMessage(
    @StringRes override val id: Int,
    override val type: SnackbarType,
    override val isClipboard: Boolean = false
) : SnackbarMessage {
    BiometryFailedToStartError(R.string.settings_error_biometry_failed_to_start, SnackbarType.ERROR),
    BiometryFailedToAuthenticateError(R.string.settings_error_biometry_failed_to_authenticate, SnackbarType.ERROR),
    FingerprintLockEnabled(R.string.settings_fingerprint_lock_enabled, SnackbarType.SUCCESS),
    FingerprintLockDisabled(R.string.settings_fingerprint_lock_disabled, SnackbarType.SUCCESS),
    ErrorPerformingOperation(R.string.settings_error_performing_operation, SnackbarType.ERROR),
    ErrorPerformingSync(R.string.settings_force_sync_error, SnackbarType.ERROR),
    SyncSuccessful(R.string.settings_force_sync_success, SnackbarType.SUCCESS),
    AppVersionCopied(R.string.settings_app_version_copied_to_clipboard, SnackbarType.SUCCESS, true)
}
