package me.proton.pass.presentation.components.model

import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ItemType
import me.proton.pass.domain.ShareId

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
