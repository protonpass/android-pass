package proton.android.pass.data.impl.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.ItemCountSummary
import proton.android.pass.data.api.repositories.ShareItemCount
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.db.entities.ItemEntity
import proton.pass.domain.ITEM_TYPE_ALIAS
import proton.pass.domain.ITEM_TYPE_LOGIN
import proton.pass.domain.ITEM_TYPE_NOTE
import proton.pass.domain.ItemId
import proton.pass.domain.ItemState
import proton.pass.domain.ShareId
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
        itemState: ItemState?,
        filter: ItemTypeFilter
    ): Flow<List<ItemEntity>> =
        if (filter == ItemTypeFilter.All) {
            database.itemsDao().observerAllForShare(userId.id, shareId.id, itemState?.value)
        } else {
            database.itemsDao()
                .observeAllForShare(userId.id, shareId.id, itemState?.value, filter.value())
        }

    override fun observeItems(
        userId: UserId,
        itemState: ItemState?,
        filter: ItemTypeFilter
    ): Flow<List<ItemEntity>> =
        if (filter == ItemTypeFilter.All) {
            database.itemsDao().observeAllForAddress(userId.id, itemState?.value)
        } else {
            database.itemsDao().observeAllForAddress(userId.id, itemState?.value, filter.value())
        }

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

    override fun observeItemCountSummary(
        userId: UserId,
        shareIds: List<ShareId>,
        itemState: ItemState?,
    ): Flow<ItemCountSummary> =
        database.itemsDao()
            .itemSummary(userId.id, shareIds.map { it.id }, itemState?.value)
            .map { values ->
                val logins = values.firstOrNull { it.itemKind == ITEM_TYPE_LOGIN }?.itemCount ?: 0
                val aliases = values.firstOrNull { it.itemKind == ITEM_TYPE_ALIAS }?.itemCount ?: 0
                val notes = values.firstOrNull { it.itemKind == ITEM_TYPE_NOTE }?.itemCount ?: 0
                ItemCountSummary(
                    total = logins + aliases + notes,
                    login = logins,
                    alias = aliases,
                    note = notes
                )
            }

    override suspend fun updateLastUsedTime(shareId: ShareId, itemId: ItemId, now: Long) {
        database.itemsDao().updateLastUsedTime(shareId.id, itemId.id, now)
    }

    override fun observeItemCount(shareIds: List<ShareId>): Flow<Map<ShareId, ShareItemCount>> =
        database.itemsDao()
            .countItemsForShares(shareIds.map { it.id })
            .map { values ->
                shareIds.associate { shareId ->
                    val rowForShare = values.firstOrNull { it.shareId == shareId.id }
                    if (rowForShare == null) {
                        shareId to ShareItemCount(0, 0)
                    } else {
                        shareId to ShareItemCount(
                            activeItems = rowForShare.activeItemCount,
                            trashedItems = rowForShare.trashedItemCount
                        )
                    }
                }
            }

    override suspend fun getItemByAliasEmail(userId: UserId, aliasEmail: String): ItemEntity? =
        database.itemsDao().getItemByAliasEmail(userId.id, aliasEmail)

    private fun ItemTypeFilter.value(): Int = when (this) {
        ItemTypeFilter.Logins -> ITEM_TYPE_LOGIN
        ItemTypeFilter.Aliases -> ITEM_TYPE_ALIAS
        ItemTypeFilter.Notes -> ITEM_TYPE_NOTE
        ItemTypeFilter.All -> throw IllegalStateException("Cannot call value to ItemTypeFilter.All")
    }

}
