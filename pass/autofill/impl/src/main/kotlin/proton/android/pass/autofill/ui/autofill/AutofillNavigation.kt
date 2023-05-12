package proton.android.pass.autofill.ui.autofill

import proton.android.pass.autofill.entities.AutofillMappings

sealed interface AutofillNavigation {
    data class Selected(val autofillMappings: AutofillMappings) : AutofillNavigation
    object Upgrade : AutofillNavigation
    object Cancel : AutofillNavigation
}
