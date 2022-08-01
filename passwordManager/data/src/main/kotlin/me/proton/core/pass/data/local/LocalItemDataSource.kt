package me.proton.core.pass.data.local

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import me.proton.core.pass.data.db.entities.ItemEntity
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ShareId

interface LocalItemDataSource {
    suspend fun upsertItem(item: ItemEntity)
    fun observeItemsForShare(userId: UserId, shareId: ShareId): Flow<List<ItemEntity>>
    fun observeItems(userId: UserId): Flow<List<ItemEntity>>
    suspend fun getById(shareId: ShareId, itemId: ItemId): ItemEntity?
    suspend fun delete(shareId: ShareId, itemId: ItemId): Boolean
    suspend fun hasItemsForShare(userId: UserId, shareId: ShareId): Boolean
}
