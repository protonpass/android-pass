package proton.android.pass.autofill.ui.autofill

import androidx.compose.runtime.Stable
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.preferences.ThemePreference

@Stable
data class AutofillAppUiState(
    val theme: ThemePreference,
    val isFingerprintRequired: Boolean,
    val itemSelected: AutofillItemSelectedState,
    val snackbarMessage: SnackbarMessage?
) {
    companion object {
        val Initial = AutofillAppUiState(
            theme = ThemePreference.System,
            isFingerprintRequired = false,
            itemSelected = AutofillItemSelectedState.Unknown,
            snackbarMessage = null
        )
    }
}
