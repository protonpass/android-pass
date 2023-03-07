package proton.android.pass.composecomponents.impl.item

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.datetime.Clock
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.pass.domain.ItemId
import proton.pass.domain.ItemType
import proton.pass.domain.ShareId

class NoteRowPreviewProvider : PreviewParameterProvider<NoteRowParameter> {
    override val values: Sequence<NoteRowParameter>
        get() = sequenceOf(
            with(title = "Empty note", text = ""),
            with(title = "This is a note item", text = "the note"),
            with(
                title = "Very long text",
                text = "this is a very long note that should become " +
                    "ellipsized if the text does not fit properly"
            ),
            with(
                title = "Very long text",
                text = "this is a very long note that should " +
                    "highlight the word monkey during the rendering",
                highlight = "monkey"
            )
        )

    companion object {
        private fun with(title: String, text: String, highlight: String = ""): NoteRowParameter =
            NoteRowParameter(
                model = ItemUiModel(
                    id = ItemId("123"),
                    shareId = ShareId("345"),
                    name = title,
                    note = "Note content",
                    itemType = ItemType.Note(text = text),
                    createTime = Clock.System.now(),
                    modificationTime = Clock.System.now(),
                    lastAutofillTime = Clock.System.now()
                ),
                highlight = highlight
            )
    }
}

data class NoteRowParameter(
    val model: ItemUiModel,
    val highlight: String
)

