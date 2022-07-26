package me.proton.core.pass.domain.repositories

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import me.proton.core.pass.domain.Item
import me.proton.core.pass.domain.ItemContents
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.Share
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.domain.ShareSelection

interface ItemRepository {
    suspend fun createItem(userId: UserId, share: Share, contents: ItemContents): Item
    fun observeItems(userId: UserId, shareSelection: ShareSelection): Flow<List<Item>>
    suspend fun deleteItem(userId: UserId, shareId: ShareId, itemId: ItemId)
}
