package me.proton.android.pass.commonuimodels.api

import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ItemType
import me.proton.pass.domain.ShareId

data class ItemUiModel(
    val id: ItemId,
    val shareId: ShareId,
    val name: String,
    val note: String,
    val itemType: ItemType
)
