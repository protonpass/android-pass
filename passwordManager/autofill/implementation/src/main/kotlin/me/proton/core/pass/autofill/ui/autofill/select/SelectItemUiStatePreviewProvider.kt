package me.proton.core.pass.autofill.ui.autofill.select

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ItemType
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.presentation.components.model.ItemUiModel

class SelectItemUiStatePreviewProvider : PreviewParameterProvider<SelectItemUiState> {
    override val values: Sequence<SelectItemUiState>
        get() = sequenceOf(
            SelectItemUiState.Loading,
            SelectItemUiState.Content(
                items = listOf(
                    ItemUiModel(
                        id = ItemId("123"),
                        shareId = ShareId("345"),
                        name = "Item with long text",
                        itemType = ItemType.Note(
                            "Some very very long test that should be ellipsized as we type"
                        )
                    )
                )
            )
        )
}
