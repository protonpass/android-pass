package me.proton.core.pass.data.local

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import me.proton.core.pass.data.db.PassDatabase
import me.proton.core.pass.data.db.entities.ItemEntity
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ShareId

class LocalItemDataSourceImpl @Inject constructor(
    private val database: PassDatabase
) : LocalItemDataSource {

    override suspend fun upsertItem(item: ItemEntity) =
        database.itemsDao().insertOrUpdate(item)

    override fun observeItemsForShare(
        userId: UserId,
        shareId: ShareId
    ): Flow<List<ItemEntity>> =
        database.itemsDao().observeAllForShare(userId.id, shareId.id)

    override fun observeItems(userId: UserId): Flow<List<ItemEntity>> =
        database.itemsDao().observeAllForAddress(userId.id)

    override suspend fun delete(shareId: ShareId, itemId: ItemId): Boolean =
        database.itemsDao().delete(shareId.id, itemId.id) > 0

    override suspend fun hasItemsForShare(userId: UserId, shareId: ShareId): Boolean =
        database.itemsDao().countItems(userId.id, shareId.id) > 0
}
