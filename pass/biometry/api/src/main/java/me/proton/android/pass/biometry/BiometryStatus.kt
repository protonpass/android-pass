package me.proton.android.pass.biometry

sealed interface BiometryStatus {
    object CanAuthenticate : BiometryStatus
    object NotEnrolled : BiometryStatus
    object NotAvailable : BiometryStatus
}
