package proton.android.pass.featureprofile.impl

import androidx.annotation.StringRes
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.notifications.api.SnackbarType

enum class ProfileSnackbarMessage(
    @StringRes override val id: Int,
    override val type: SnackbarType,
    override val isClipboard: Boolean = false
) : SnackbarMessage {
    BiometryFailedToStartError(R.string.profile_error_biometry_failed_to_start, SnackbarType.ERROR),
    BiometryFailedToAuthenticateError(R.string.profile_error_biometry_failed_to_authenticate, SnackbarType.ERROR),
    FingerprintLockEnabled(R.string.profile_fingerprint_lock_enabled, SnackbarType.SUCCESS),
    FingerprintLockDisabled(R.string.profile_fingerprint_lock_disabled, SnackbarType.SUCCESS),
    ErrorPerformingOperation(R.string.profile_error_performing_operation, SnackbarType.ERROR)
}
