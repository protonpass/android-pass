package proton.android.pass.data.api

import me.proton.core.domain.entity.UserId
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

data class SearchEntry(
    val itemId: ItemId,
    val shareId: ShareId,
    val userId: UserId,
    val createTime: Long
)
