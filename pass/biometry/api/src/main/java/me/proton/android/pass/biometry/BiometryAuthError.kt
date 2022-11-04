package me.proton.android.pass.biometry

sealed interface BiometryAuthError {
    object HardwareUnavailable : BiometryAuthError
    object UnableToProcess : BiometryAuthError
    object Timeout : BiometryAuthError
    object NoSpace : BiometryAuthError
    object Canceled : BiometryAuthError
    object Lockout : BiometryAuthError
    object Vendor : BiometryAuthError
    object LockoutPermanent : BiometryAuthError
    object UserCanceled : BiometryAuthError
    object NoBiometrics : BiometryAuthError
    object HardwareNotPresent : BiometryAuthError
    object NegativeButton : BiometryAuthError
    object NoDeviceCredential : BiometryAuthError
    object Unknown : BiometryAuthError

    companion object
}
