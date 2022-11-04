package me.proton.pass.autofill.ui.autofill

import me.proton.android.pass.preferences.ThemePreference

data class AutofillAppUiState(
    val theme: ThemePreference,
    val isFingerprintRequired: Boolean
) {
    companion object {
        val Initial = AutofillAppUiState(
            theme = ThemePreference.System,
            isFingerprintRequired = false
        )
    }
}
