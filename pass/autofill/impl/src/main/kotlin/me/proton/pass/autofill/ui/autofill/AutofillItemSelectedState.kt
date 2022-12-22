package me.proton.pass.autofill.ui.autofill

import me.proton.pass.autofill.entities.AutofillMappings

sealed interface AutofillItemSelectedState {
    object Unknown : AutofillItemSelectedState
    data class Selected(val item: AutofillMappings) : AutofillItemSelectedState
}
