package me.proton.core.pass.autofill.ui.autofill.select

import androidx.compose.runtime.Immutable
import me.proton.core.pass.autofill.entities.AutofillItem
import me.proton.core.pass.presentation.components.model.ItemUiModel

sealed class SelectItemUiState {
    object Loading : SelectItemUiState()

    @Immutable
    data class Content(
        val items: List<ItemUiModel>
    ) : SelectItemUiState()

    data class Selected(val autofillItem: AutofillItem) : SelectItemUiState()
    data class Error(val message: String) : SelectItemUiState()
}
