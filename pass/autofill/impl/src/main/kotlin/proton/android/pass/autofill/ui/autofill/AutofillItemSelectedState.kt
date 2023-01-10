package proton.android.pass.autofill.ui.autofill

import proton.android.pass.autofill.entities.AutofillMappings

sealed interface AutofillItemSelectedState {
    object Unknown : AutofillItemSelectedState
    data class Selected(val item: AutofillMappings) : AutofillItemSelectedState
}
