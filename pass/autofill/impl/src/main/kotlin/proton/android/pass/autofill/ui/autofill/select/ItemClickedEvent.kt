package proton.android.pass.autofill.ui.autofill.select

import proton.android.pass.autofill.entities.AutofillItem

sealed interface ItemClickedEvent {
    object None : ItemClickedEvent
    data class Clicked(val item: AutofillItem) : ItemClickedEvent
}
