package me.proton.android.pass.data.impl.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import me.proton.android.pass.data.impl.db.entities.ItemEntity
import me.proton.android.pass.data.impl.local.LocalItemDataSource
import me.proton.core.domain.entity.UserId
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ItemState
import me.proton.pass.domain.ShareId

class TestLocalItemDataSource : LocalItemDataSource {

    private val memory: MutableList<ItemEntity> = mutableListOf()

    fun getMemory(): List<ItemEntity> = memory

    override suspend fun upsertItem(item: ItemEntity) {
        memory.add(item)
    }

    override suspend fun upsertItems(items: List<ItemEntity>) {
        memory.addAll(items)
    }

    override fun observeItemsForShare(
        userId: UserId,
        shareId: ShareId,
        itemState: ItemState
    ): Flow<List<ItemEntity>> = flowOf(memory)

    override fun observeItems(userId: UserId, itemState: ItemState): Flow<List<ItemEntity>> {
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
}
