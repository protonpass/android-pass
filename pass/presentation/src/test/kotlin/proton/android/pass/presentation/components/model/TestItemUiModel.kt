package proton.android.pass.presentation.components.model

import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.pass.domain.ItemId
import proton.pass.domain.ItemType
import proton.pass.domain.ShareId

object TestItemUiModel {

    fun create(
        title: String = "item-title",
        note: String = "item-note",
        itemType: ItemType = ItemType.Password
    ): ItemUiModel {
        return ItemUiModel(
            id = ItemId(id = "item-id"),
            shareId = ShareId(id = "share-id"),
            itemType = itemType,
            name = title,
            note = note
        )
    }
}
