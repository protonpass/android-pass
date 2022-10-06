package me.proton.core.pass.presentation.components.previewproviders

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ItemType
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.presentation.components.model.ItemUiModel

class ItemUiModelPreviewProvider : PreviewParameterProvider<ItemUiModel> {
    override val values: Sequence<ItemUiModel>
        get() = sequenceOf(
            ItemUiModel(
                id = ItemId("123"),
                shareId = ShareId("345"),
                name = "Item with long text",
                itemType = ItemType.Note(
                    "Some very very long test that should be ellipsized as we type"
                )
            ),
            ItemUiModel(
                id = ItemId("123"),
                shareId = ShareId("345"),
                name = "Item with multiline text",
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
