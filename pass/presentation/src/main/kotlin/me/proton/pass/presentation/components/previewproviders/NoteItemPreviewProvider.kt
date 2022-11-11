package me.proton.pass.presentation.components.previewproviders

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ItemType
import me.proton.pass.domain.ShareId
import me.proton.pass.presentation.components.model.ItemUiModel

class NoteItemPreviewProvider : PreviewParameterProvider<NoteItemParameter> {
    override val values: Sequence<NoteItemParameter>
        get() = sequenceOf(
            with(title = "Empty note", text = ""),
            with(title = "This is a note item", text = "the note"),
            with(
                title = "Very long text",
                text = "this is a very long note that should become " +
                    "ellipsized if the text does not fit properly"
            )
        )

    companion object {
        private fun with(title: String, text: String): NoteItemParameter =
            NoteItemParameter(
                model = ItemUiModel(
                    id = ItemId("123"),
                    shareId = ShareId("345"),
                    name = title,
                    itemType = ItemType.Note(text = text)
                ),
                itemType = ItemType.Note(text = text)
            )

    }
}

data class NoteItemParameter(
    val model: ItemUiModel,
    val itemType: ItemType.Note
)

