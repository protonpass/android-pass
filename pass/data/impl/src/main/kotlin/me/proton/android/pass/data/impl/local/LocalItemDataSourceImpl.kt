package me.proton.android.pass.data.impl.local

import kotlinx.coroutines.flow.Flow
import me.proton.android.pass.data.impl.db.PassDatabase
import me.proton.android.pass.data.impl.db.entities.ItemEntity
import me.proton.core.domain.entity.UserId
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ItemState
import me.proton.pass.domain.ShareId
import javax.inject.Inject

class LocalItemDataSourceImpl @Inject constructor(
    private val database: PassDatabase
) : LocalItemDataSource {

    override suspend fun upsertItem(item: ItemEntity) = upsertItems(listOf(item))

    override suspend fun upsertItems(items: List<ItemEntity>) =
        database.itemsDao().insertOrUpdate(*items.toTypedArray())

    override fun observeItemsForShare(
        userId: UserId,
        shareId: ShareId,
        itemState: ItemState
    ): Flow<List<ItemEntity>> =
        database.itemsDao().observerAllForShare(userId.id, shareId.id, itemState.value)

    override fun observeItems(userId: UserId, itemState: ItemState): Flow<List<ItemEntity>> =
        database.itemsDao().observeAllForAddress(userId.id, itemState.value)

    override suspend fun getById(shareId: ShareId, itemId: ItemId): ItemEntity? =
        database.itemsDao().getById(shareId.id, itemId.id)

    override suspend fun setItemState(shareId: ShareId, itemId: ItemId, itemState: ItemState) =
        database.itemsDao().setItemState(shareId.id, itemId.id, itemState.value)

    override suspend fun getTrashedItems(userId: UserId): List<ItemEntity> =
        database.itemsDao().getItemsWithState(userId.id, ItemState.Trashed.value)

    override suspend fun delete(shareId: ShareId, itemId: ItemId): Boolean =
        database.itemsDao().delete(shareId.id, itemId.id) > 0

    override suspend fun hasItemsForShare(userId: UserId, shareId: ShareId): Boolean =
        database.itemsDao().countItems(userId.id, shareId.id) > 0
}
