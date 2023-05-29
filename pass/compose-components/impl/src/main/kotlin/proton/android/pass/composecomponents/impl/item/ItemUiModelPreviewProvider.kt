package proton.android.pass.composecomponents.impl.item

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.datetime.Clock
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.pass.domain.ItemContents
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

class ItemUiModelPreviewProvider : PreviewParameterProvider<ItemUiModel> {
    override val values: Sequence<ItemUiModel>
        get() = sequenceOf(
            ItemUiModel(
                id = ItemId("123"),
                shareId = ShareId("345"),
                contents = ItemContents.Note(
                    "Item with long text",
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
                contents = ItemContents.Note(
                    "Item with multiline text",
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
