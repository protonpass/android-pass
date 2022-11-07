package me.proton.android.pass

import me.proton.android.pass.preferences.BiometricLockState

sealed interface AuthRequiredState {
    object Loading : AuthRequiredState
    data class Value(val state: BiometricLockState) : AuthRequiredState
}
