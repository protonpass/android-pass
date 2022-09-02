package me.proton.core.pass.domain.repositories

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import me.proton.core.pass.domain.Item
import me.proton.core.pass.domain.ItemContents
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ItemState
import me.proton.core.pass.domain.Share
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.domain.ShareSelection
import me.proton.core.pass.domain.entity.NewAlias

interface ItemRepository {
    suspend fun createItem(userId: UserId, share: Share, contents: ItemContents): Item
    suspend fun createAlias(userId: UserId, share: Share, newAlias: NewAlias): Item
    suspend fun updateItem(userId: UserId, share: Share, item: Item, contents: ItemContents): Item
    fun observeItems(userId: UserId, shareSelection: ShareSelection, itemState: ItemState): Flow<List<Item>>
    suspend fun getById(userId: UserId, shareId: ShareId, itemId: ItemId): Item
    suspend fun trashItem(userId: UserId, shareId: ShareId, itemId: ItemId)
    suspend fun restoreItem(userId: UserId, shareId: ShareId, itemId: ItemId)
    suspend fun deleteItem(userId: UserId, shareId: ShareId, itemId: ItemId)
    suspend fun clearTrash(userId: UserId)
}
