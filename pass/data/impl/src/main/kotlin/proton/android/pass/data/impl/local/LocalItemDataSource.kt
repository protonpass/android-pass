package proton.android.pass.data.impl.local

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.ItemCountSummary
import proton.android.pass.data.api.repositories.ShareItemCount
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.impl.db.entities.ItemEntity
import proton.pass.domain.ItemId
import proton.pass.domain.ItemState
import proton.pass.domain.ShareId

interface LocalItemDataSource {
    suspend fun upsertItem(item: ItemEntity)
    suspend fun upsertItems(items: List<ItemEntity>)
    fun observeItemsForShare(
        userId: UserId,
        shareId: ShareId,
        itemState: ItemState,
        filter: ItemTypeFilter
    ): Flow<List<ItemEntity>>
    fun observeItems(
        userId: UserId,
        itemState: ItemState,
        filter: ItemTypeFilter
    ): Flow<List<ItemEntity>>
    suspend fun getById(shareId: ShareId, itemId: ItemId): ItemEntity?
    suspend fun setItemState(shareId: ShareId, itemId: ItemId, itemState: ItemState)
    suspend fun getTrashedItems(userId: UserId): List<ItemEntity>
    suspend fun delete(shareId: ShareId, itemId: ItemId): Boolean
    suspend fun hasItemsForShare(userId: UserId, shareId: ShareId): Boolean
    fun observeItemCountSummary(
        userId: UserId,
        shareIds: List<ShareId>
    ): Flow<ItemCountSummary>
    suspend fun updateLastUsedTime(shareId: ShareId, itemId: ItemId, now: Long)
    fun observeItemCount(shareIds: List<ShareId>): Flow<Map<ShareId, ShareItemCount>>
}
