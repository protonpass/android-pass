package me.proton.android.pass.data.api.repositories

import kotlinx.coroutines.flow.Flow
import me.proton.android.pass.data.api.ItemCountSummary
import me.proton.android.pass.data.api.PendingEventList
import me.proton.android.pass.data.api.usecases.ItemTypeFilter
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import me.proton.pass.common.api.Option
import me.proton.pass.common.api.Result
import me.proton.pass.domain.Item
import me.proton.pass.domain.ItemContents
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ItemState
import me.proton.pass.domain.Share
import me.proton.pass.domain.ShareId
import me.proton.pass.domain.ShareSelection
import me.proton.pass.domain.entity.NewAlias
import me.proton.pass.domain.entity.PackageName

interface ItemRepository {
    suspend fun createItem(
        userId: UserId,
        share: Share,
        contents: ItemContents,
        packageName: PackageName? = null
    ): Result<Item>
    suspend fun createAlias(userId: UserId, share: Share, newAlias: NewAlias): Result<Item>
    suspend fun updateItem(
        userId: UserId,
        share: Share,
        item: Item,
        contents: ItemContents
    ): Result<Item>

    fun observeItems(
        userId: UserId,
        shareSelection: ShareSelection,
        itemState: ItemState,
        itemTypeFilter: ItemTypeFilter = ItemTypeFilter.All
    ): Flow<Result<List<Item>>>

    suspend fun getById(userId: UserId, shareId: ShareId, itemId: ItemId): Result<Item>
    suspend fun trashItem(userId: UserId, shareId: ShareId, itemId: ItemId): Result<Unit>
    suspend fun untrashItem(userId: UserId, shareId: ShareId, itemId: ItemId): Result<Unit>
    suspend fun deleteItem(userId: UserId, shareId: ShareId, itemId: ItemId): Result<Unit>
    suspend fun clearTrash(userId: UserId): Result<Unit>
    suspend fun restoreItems(userId: UserId): Result<Unit>
    suspend fun addPackageAndUrlToItem(
        shareId: ShareId,
        itemId: ItemId,
        packageName: Option<PackageName>,
        url: Option<String>
    ): Result<Item>

    suspend fun refreshItems(
        userId: UserId,
        share: Share
    ): Result<List<Item>>

    suspend fun refreshItems(
        userId: UserId,
        shareId: ShareId
    ): Result<List<Item>>

    suspend fun applyEvents(
        userId: UserId,
        addressId: AddressId,
        shareId: ShareId,
        events: PendingEventList
    )

    fun observeItemCountSummary(
        userId: UserId,
        shareId: ShareId
    ): Flow<ItemCountSummary>

    suspend fun updateItemLastUsed(
        shareId: ShareId,
        itemId: ItemId
    )
}
