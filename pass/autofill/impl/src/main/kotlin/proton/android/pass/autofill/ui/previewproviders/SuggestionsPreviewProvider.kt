package proton.android.pass.autofill.ui.previewproviders

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.pass.domain.ItemId
import proton.pass.domain.ItemType
import proton.pass.domain.ShareId

class SuggestionsPreviewProvider : PreviewParameterProvider<List<ItemUiModel>> {
    override val values: Sequence<List<ItemUiModel>>
        get() = sequenceOf(
            listOf(
                item("item 1", "some username"),
                item("item 2", "other username")
            )
        )

    private fun item(name: String, username: String): ItemUiModel =
        ItemUiModel(
            id = ItemId(name),
            shareId = ShareId(name),
            name = name,
            note = "",
            itemType = ItemType.Login(
                username = username,
                password = "",
                websites = emptyList()
            )
        )
}
