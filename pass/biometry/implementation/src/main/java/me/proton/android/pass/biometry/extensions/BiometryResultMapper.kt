package me.proton.android.pass.biometry.extensions

import androidx.biometric.BiometricManager
import me.proton.android.pass.biometry.BiometryResult
import me.proton.android.pass.biometry.BiometryStartupError

@Suppress("ComplexMethod")
fun BiometryResult.Companion.from(value: Int): BiometryResult =
    when (value) {
        BiometricManager.BIOMETRIC_SUCCESS -> BiometryResult.Success
        BiometricManager.BIOMETRIC_STATUS_UNKNOWN ->
            BiometryResult.FailedToStart(BiometryStartupError.Unknown)
        BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED ->
            BiometryResult.FailedToStart(BiometryStartupError.Unsupported)
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
            BiometryResult.FailedToStart(BiometryStartupError.HardwareUnavailable)
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
            BiometryResult.FailedToStart(BiometryStartupError.NoneEnrolled)
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
            BiometryResult.FailedToStart(BiometryStartupError.NoHardware)
        BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED ->
            BiometryResult.FailedToStart(BiometryStartupError.SecurityUpdateRequired)
        else -> BiometryResult.FailedToStart(BiometryStartupError.Unknown)
    }
