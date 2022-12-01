package me.proton.pass.presentation.components.previewproviders

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ItemType
import me.proton.pass.domain.ShareId
import me.proton.pass.presentation.components.model.ItemUiModel

class ItemUiModelPreviewProvider : PreviewParameterProvider<ItemUiModel> {
    override val values: Sequence<ItemUiModel>
        get() = sequenceOf(
            ItemUiModel(
                id = ItemId("123"),
                shareId = ShareId("345"),
                name = "Item with long text",
                note = "Note content",
                itemType = ItemType.Note(
                    "Some very very long test that should be ellipsized as we type"
                )
            ),
            ItemUiModel(
                id = ItemId("123"),
                shareId = ShareId("345"),
                name = "Item with multiline text",
                note = "Note content",
                itemType = ItemType.Note(
                    """
                A line
                Another line
                At some point this gets ellipsized
                    """.trimIndent()
                )
            )
        )
}
