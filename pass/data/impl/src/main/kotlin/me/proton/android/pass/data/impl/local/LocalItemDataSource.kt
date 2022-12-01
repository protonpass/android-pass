package me.proton.android.pass.data.impl.local

import kotlinx.coroutines.flow.Flow
import me.proton.android.pass.data.impl.db.entities.ItemEntity
import me.proton.core.domain.entity.UserId
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ItemState
import me.proton.pass.domain.ShareId

interface LocalItemDataSource {
    suspend fun upsertItem(item: ItemEntity)
    suspend fun upsertItems(items: List<ItemEntity>)
    fun observeItemsForShare(userId: UserId, shareId: ShareId, itemState: ItemState): Flow<List<ItemEntity>>
    fun observeItems(userId: UserId, itemState: ItemState): Flow<List<ItemEntity>>
    suspend fun getById(shareId: ShareId, itemId: ItemId): ItemEntity?
    suspend fun setItemState(shareId: ShareId, itemId: ItemId, itemState: ItemState)
    suspend fun getTrashedItems(userId: UserId): List<ItemEntity>
    suspend fun delete(shareId: ShareId, itemId: ItemId): Boolean
    suspend fun hasItemsForShare(userId: UserId, shareId: ShareId): Boolean
}
