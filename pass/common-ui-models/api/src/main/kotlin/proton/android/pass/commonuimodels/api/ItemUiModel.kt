package proton.android.pass.commonuimodels.api

import kotlinx.datetime.Instant
import proton.pass.domain.ItemId
import proton.pass.domain.ItemType
import proton.pass.domain.ShareId

data class ItemUiModel(
    val id: ItemId,
    val shareId: ShareId,
    val name: String,
    val note: String,
    val itemType: ItemType,
    val state: Int,
    val createTime: Instant,
    val modificationTime: Instant,
    val lastAutofillTime: Instant?
)
