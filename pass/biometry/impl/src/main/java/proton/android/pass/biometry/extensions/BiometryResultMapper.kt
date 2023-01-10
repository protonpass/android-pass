package proton.android.pass.biometry.extensions

import androidx.biometric.BiometricManager
import proton.android.pass.biometry.BiometryResult

@Suppress("ComplexMethod")
fun BiometryResult.Companion.from(value: Int): BiometryResult =
    when (value) {
        BiometricManager.BIOMETRIC_SUCCESS -> BiometryResult.Success
        BiometricManager.BIOMETRIC_STATUS_UNKNOWN ->
            BiometryResult.FailedToStart(proton.android.pass.biometry.BiometryStartupError.Unknown)
        BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED ->
            BiometryResult.FailedToStart(proton.android.pass.biometry.BiometryStartupError.Unsupported)
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
            BiometryResult.FailedToStart(proton.android.pass.biometry.BiometryStartupError.HardwareUnavailable)
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
            BiometryResult.FailedToStart(proton.android.pass.biometry.BiometryStartupError.NoneEnrolled)
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
            BiometryResult.FailedToStart(proton.android.pass.biometry.BiometryStartupError.NoHardware)
        BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED ->
            BiometryResult.FailedToStart(proton.android.pass.biometry.BiometryStartupError.SecurityUpdateRequired)
        else -> BiometryResult.FailedToStart(proton.android.pass.biometry.BiometryStartupError.Unknown)
    }
