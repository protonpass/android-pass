package proton.android.pass.data.impl.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.ItemCountSummary
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.impl.db.entities.ItemEntity
import proton.android.pass.data.impl.local.LocalItemDataSource
import proton.pass.domain.ItemId
import proton.pass.domain.ItemState
import proton.pass.domain.ShareId

class TestLocalItemDataSource : LocalItemDataSource {

    private val memory: MutableList<ItemEntity> = mutableListOf()
    private var summary: MutableStateFlow<ItemCountSummary> = MutableStateFlow(ItemCountSummary.Initial)

    fun getMemory(): List<ItemEntity> = memory

    fun emitSummary(value: ItemCountSummary) {
        summary.tryEmit(value)
    }

    override suspend fun upsertItem(item: ItemEntity) {
        memory.add(item)
    }

    override suspend fun upsertItems(items: List<ItemEntity>) {
        memory.addAll(items)
    }

    override fun observeItemsForShare(
        userId: UserId,
        shareId: ShareId,
        itemState: ItemState,
        filter: ItemTypeFilter
    ): Flow<List<ItemEntity>> = flowOf(memory)

    override fun observeItems(
        userId: UserId,
        itemState: ItemState,
        filter: ItemTypeFilter
    ): Flow<List<ItemEntity>> {
        throw IllegalStateException("Not yet implemented")
    }

    override suspend fun getById(shareId: ShareId, itemId: ItemId): ItemEntity? {
        throw IllegalStateException("Not yet implemented")
    }

    override suspend fun setItemState(shareId: ShareId, itemId: ItemId, itemState: ItemState) {
        throw IllegalStateException("Not yet implemented")
    }

    override suspend fun getTrashedItems(userId: UserId): List<ItemEntity> {
        throw IllegalStateException("Not yet implemented")
    }

    override suspend fun delete(shareId: ShareId, itemId: ItemId): Boolean {
        throw IllegalStateException("Not yet implemented")
    }

    override suspend fun hasItemsForShare(userId: UserId, shareId: ShareId): Boolean {
        throw IllegalStateException("Not yet implemented")
    }

    override fun observeItemCountSummary(
        userId: UserId,
        shareIds: List<ShareId>
    ): Flow<ItemCountSummary> = summary

    override suspend fun updateLastUsedTime(shareId: ShareId, itemId: ItemId, now: Long) {
        throw IllegalStateException("Not yet implemented")
    }
}
