package me.proton.pass.autofill.ui.autofill

import me.proton.android.pass.notifications.api.SnackbarMessage
import me.proton.android.pass.preferences.ThemePreference
import me.proton.pass.common.api.None
import me.proton.pass.common.api.Option

data class AutofillAppUiState(
    val theme: ThemePreference,
    val isFingerprintRequired: Boolean,
    val itemSelected: AutofillItemSelectedState,
    val snackbarMessage: Option<SnackbarMessage>
) {
    companion object {
        val Initial = AutofillAppUiState(
            theme = ThemePreference.System,
            isFingerprintRequired = false,
            itemSelected = AutofillItemSelectedState.Unknown,
            snackbarMessage = None
        )
    }
}
