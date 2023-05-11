package proton.android.pass.data.api.repositories

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import proton.android.pass.common.api.Option
import proton.android.pass.data.api.ItemCountSummary
import proton.android.pass.data.api.PendingEventList
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.pass.domain.Item
import proton.pass.domain.ItemContents
import proton.pass.domain.ItemId
import proton.pass.domain.ItemState
import proton.pass.domain.Share
import proton.pass.domain.ShareId
import proton.pass.domain.ShareSelection
import proton.pass.domain.entity.NewAlias
import proton.pass.domain.entity.PackageInfo

data class ShareItemCount(
    val activeItems: Long,
    val trashedItems: Long
)

interface ItemRepository {
    suspend fun createItem(
        userId: UserId,
        share: Share,
        contents: ItemContents
    ): Item

    suspend fun createAlias(userId: UserId, share: Share, newAlias: NewAlias): Item
    suspend fun createItemAndAlias(
        userId: UserId,
        shareId: ShareId,
        contents: ItemContents,
        newAlias: NewAlias
    ): Item

    suspend fun updateItem(
        userId: UserId,
        share: Share,
        item: Item,
        contents: ItemContents
    ): Item

    fun observeItems(
        userId: UserId,
        shareSelection: ShareSelection,
        itemState: ItemState?,
        itemTypeFilter: ItemTypeFilter = ItemTypeFilter.All
    ): Flow<List<Item>>

    suspend fun getById(userId: UserId, shareId: ShareId, itemId: ItemId): Item
    suspend fun trashItem(userId: UserId, shareId: ShareId, itemId: ItemId)
    suspend fun untrashItem(userId: UserId, shareId: ShareId, itemId: ItemId)
    suspend fun deleteItem(userId: UserId, shareId: ShareId, itemId: ItemId)
    suspend fun clearTrash(userId: UserId)
    suspend fun restoreItems(userId: UserId)
    suspend fun addPackageAndUrlToItem(
        shareId: ShareId,
        itemId: ItemId,
        packageInfo: Option<PackageInfo>,
        url: Option<String>
    ): Item

    suspend fun refreshItems(
        userId: UserId,
        share: Share
    ): List<Item>

    suspend fun refreshItems(
        userId: UserId,
        shareId: ShareId
    ): List<Item>

    suspend fun applyEvents(
        userId: UserId,
        addressId: AddressId,
        shareId: ShareId,
        events: PendingEventList
    )

    fun observeItemCountSummary(
        userId: UserId,
        shareIds: List<ShareId>,
        itemState: ItemState?
    ): Flow<ItemCountSummary>

    fun observeItemCount(
        shareIds: List<ShareId>
    ): Flow<Map<ShareId, ShareItemCount>>

    suspend fun updateItemLastUsed(
        shareId: ShareId,
        itemId: ItemId
    )

    suspend fun migrateItem(
        userId: UserId,
        source: Share,
        destination: Share,
        itemId: ItemId
    ): Item

    suspend fun getItemByAliasEmail(
        userId: UserId,
        aliasEmail: String
    ): Item?
}
