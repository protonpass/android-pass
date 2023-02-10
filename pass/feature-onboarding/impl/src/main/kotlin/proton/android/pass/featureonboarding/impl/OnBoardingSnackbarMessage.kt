package proton.android.pass.featureonboarding.impl

import androidx.annotation.StringRes
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.notifications.api.SnackbarType

enum class OnBoardingSnackbarMessage(
    @StringRes override val id: Int,
    override val type: SnackbarType,
    override val isClipboard: Boolean = false
) : SnackbarMessage {
    BiometryFailedToStartError(R.string.error_biometry_failed_to_start, SnackbarType.ERROR),
    BiometryFailedToAuthenticateError(R.string.error_biometry_failed_to_authenticate, SnackbarType.ERROR),
    FingerprintLockEnabled(R.string.settings_fingerprint_lock_enabled, SnackbarType.SUCCESS),
    ErrorPerformingOperation(R.string.error_performing_operation, SnackbarType.ERROR)
}
