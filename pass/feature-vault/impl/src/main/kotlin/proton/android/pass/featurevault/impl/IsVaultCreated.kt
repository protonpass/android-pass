package proton.android.pass.featurevault.impl

import androidx.compose.runtime.Stable

@Stable
sealed interface VaultSavedState {
    object Unknown : VaultSavedState
    object Success : VaultSavedState
}
