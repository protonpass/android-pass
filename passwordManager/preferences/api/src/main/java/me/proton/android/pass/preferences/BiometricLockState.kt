package me.proton.android.pass.preferences

sealed interface BiometricLockState {
    object Enabled : BiometricLockState
    object Disabled : BiometricLockState
}
