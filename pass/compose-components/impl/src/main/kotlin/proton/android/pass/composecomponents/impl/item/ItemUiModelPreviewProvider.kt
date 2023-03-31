package proton.android.pass.composecomponents.impl.item

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.datetime.Clock
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.pass.domain.ItemId
import proton.pass.domain.ItemType
import proton.pass.domain.ShareId

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
                ),
                state = 0,
                createTime = Clock.System.now(),
                modificationTime = Clock.System.now(),
                lastAutofillTime = Clock.System.now()
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
                ),
                state = 0,
                createTime = Clock.System.now(),
                modificationTime = Clock.System.now(),
                lastAutofillTime = Clock.System.now()
            )
        )
}
