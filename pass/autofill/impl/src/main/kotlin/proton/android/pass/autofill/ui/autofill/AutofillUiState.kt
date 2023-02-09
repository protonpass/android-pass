package proton.android.pass.autofill.ui.autofill

import proton.android.pass.autofill.entities.AutofillAppState
import proton.android.pass.autofill.entities.AutofillItem
import proton.android.pass.common.api.Option

sealed interface AutofillUiState {

    data class StartAutofillUiState(
        val autofillAppState: AutofillAppState,
        val themePreference: Int,
        val isFingerprintRequiredPreference: Boolean,
        val copyTotpToClipboardPreference: Boolean,
        val selectedAutofillItem: Option<AutofillItem>
    ) : AutofillUiState

    object UninitialisedAutofillUiState : AutofillUiState
    object NotValidAutofillUiState : AutofillUiState
}
