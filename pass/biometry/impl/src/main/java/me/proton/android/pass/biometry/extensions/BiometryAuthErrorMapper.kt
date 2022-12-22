package me.proton.android.pass.biometry.extensions

import androidx.biometric.BiometricPrompt
import me.proton.android.pass.biometry.BiometryAuthError

@Suppress("ComplexMethod")
fun BiometryAuthError.Companion.from(value: Int): BiometryAuthError =
    when (value) {
        BiometricPrompt.ERROR_HW_UNAVAILABLE -> BiometryAuthError.HardwareUnavailable
        BiometricPrompt.ERROR_UNABLE_TO_PROCESS -> BiometryAuthError.UnableToProcess
        BiometricPrompt.ERROR_TIMEOUT -> BiometryAuthError.Timeout
        BiometricPrompt.ERROR_NO_SPACE -> BiometryAuthError.NoSpace
        BiometricPrompt.ERROR_CANCELED -> BiometryAuthError.Canceled
        BiometricPrompt.ERROR_LOCKOUT -> BiometryAuthError.Lockout
        BiometricPrompt.ERROR_VENDOR -> BiometryAuthError.Vendor
        BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> BiometryAuthError.LockoutPermanent
        BiometricPrompt.ERROR_USER_CANCELED -> BiometryAuthError.UserCanceled
        BiometricPrompt.ERROR_NO_BIOMETRICS -> BiometryAuthError.NoBiometrics
        BiometricPrompt.ERROR_HW_NOT_PRESENT -> BiometryAuthError.HardwareNotPresent
        BiometricPrompt.ERROR_NEGATIVE_BUTTON -> BiometryAuthError.NegativeButton
        BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL -> BiometryAuthError.NoDeviceCredential
        else -> BiometryAuthError.Unknown
    }


