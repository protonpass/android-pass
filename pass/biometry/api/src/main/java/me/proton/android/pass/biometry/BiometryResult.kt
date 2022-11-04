package me.proton.android.pass.biometry

sealed interface BiometryResult {
    object Success : BiometryResult
    object Failed : BiometryResult

    data class Error(val cause: BiometryAuthError) : BiometryResult
    data class FailedToStart(val cause: BiometryStartupError) : BiometryResult

    companion object
}
