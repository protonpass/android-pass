package proton.android.pass.autofill.ui.autofill.select

import proton.android.pass.autofill.entities.AutofillMappings

sealed interface AutofillItemClickedEvent {
    object None : AutofillItemClickedEvent
    data class Clicked(val autofillMappings: AutofillMappings) : AutofillItemClickedEvent
}
