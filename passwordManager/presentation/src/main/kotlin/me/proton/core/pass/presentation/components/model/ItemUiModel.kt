package me.proton.core.pass.presentation.components.model

import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ItemType
import me.proton.core.pass.domain.ShareId

data class ItemUiModel(
    val id: ItemId,
    val shareId: ShareId,
    val name: String,
    val itemType: ItemType
)
