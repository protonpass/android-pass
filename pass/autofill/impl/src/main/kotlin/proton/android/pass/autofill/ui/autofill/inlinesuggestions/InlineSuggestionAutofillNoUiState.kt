package proton.android.pass.autofill.ui.autofill.inlinesuggestions

import proton.android.pass.autofill.entities.AutofillMappings

sealed interface InlineSuggestionAutofillNoUiState {
    data class Success(val autofillMappings: AutofillMappings) : InlineSuggestionAutofillNoUiState
    object Error : InlineSuggestionAutofillNoUiState
    object NotInitialised : InlineSuggestionAutofillNoUiState
}
