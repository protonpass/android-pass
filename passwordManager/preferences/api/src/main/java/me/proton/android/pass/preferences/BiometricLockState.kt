package me.proton.android.pass.preferences

sealed interface BiometricLockState {
    object Enabled : BiometricLockState
    object Disabled : BiometricLockState

    companion object {
        fun from(value: Boolean): BiometricLockState = if (value) { Enabled } else { Disabled }
    }
}

fun BiometricLockState.value(): Boolean =
    when (this) {
        BiometricLockState.Enabled -> true
        BiometricLockState.Disabled -> false
    }


