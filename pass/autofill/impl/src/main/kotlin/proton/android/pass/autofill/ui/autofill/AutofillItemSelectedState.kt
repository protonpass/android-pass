package proton.android.pass.autofill.ui.autofill

import proton.android.pass.autofill.entities.AutofillItem

sealed interface AutofillItemSelectedState {
    object Unknown : AutofillItemSelectedState
    data class Selected(val item: AutofillItem) : AutofillItemSelectedState
}
