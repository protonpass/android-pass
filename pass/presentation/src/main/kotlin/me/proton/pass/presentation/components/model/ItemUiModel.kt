package me.proton.pass.presentation.components.model

import androidx.compose.runtime.Stable
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ItemType
import me.proton.pass.domain.ShareId

@Stable
data class ItemUiModel(
    val id: ItemId,
    val shareId: ShareId,
    val name: String,
    val note: String,
    val itemType: ItemType
)
