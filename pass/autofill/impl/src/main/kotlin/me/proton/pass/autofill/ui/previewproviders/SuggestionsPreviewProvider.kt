package me.proton.pass.autofill.ui.previewproviders

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import me.proton.android.pass.commonuimodels.api.ItemUiModel
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ItemType
import me.proton.pass.domain.ShareId

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
