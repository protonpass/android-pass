package proton.android.pass.biometry

sealed interface BiometryStartupError {
    object Unknown : BiometryStartupError
    object Unsupported : BiometryStartupError
    object HardwareUnavailable : BiometryStartupError
    object NoneEnrolled : BiometryStartupError
    object NoHardware : BiometryStartupError
    object SecurityUpdateRequired : BiometryStartupError

    companion object
}
