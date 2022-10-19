package me.proton.core.pass.autofill.ui.autofill.select

import me.proton.core.pass.autofill.entities.AutofillItem

sealed interface ItemClickedEvent {
    object None : ItemClickedEvent
    data class Clicked(val item: AutofillItem) : ItemClickedEvent
}
