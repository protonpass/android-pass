package proton.android.pass.data.fakes.repositories

import kotlinx.coroutines.flow.Flow
import proton.android.pass.data.api.ItemCountSummary
import proton.android.pass.data.api.PendingEventList
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.usecases.ItemTypeFilter
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Result
import proton.pass.domain.Item
import proton.pass.domain.ItemContents
import proton.pass.domain.ItemId
import proton.pass.domain.ItemState
import proton.pass.domain.Share
import proton.pass.domain.ShareId
import proton.pass.domain.ShareSelection
import proton.pass.domain.entity.NewAlias
import proton.pass.domain.entity.PackageName
import javax.inject.Inject

@Suppress("NotImplementedDeclaration")
class TestItemRepository @Inject constructor() : ItemRepository {

    override suspend fun createItem(
        userId: UserId,
        share: Share,
        contents: ItemContents,
        packageName: PackageName?
    ): Result<Item> {
        TODO("Not yet implemented")
    }

    override suspend fun createAlias(
        userId: UserId,
        share: Share,
        newAlias: NewAlias
    ): Result<Item> {
        TODO("Not yet implemented")
    }

    override suspend fun updateItem(
        userId: UserId,
        share: Share,
        item: Item,
        contents: ItemContents
    ): Result<Item> {
        TODO("Not yet implemented")
    }

    override fun observeItems(
        userId: UserId,
        shareSelection: ShareSelection,
        itemState: ItemState,
        itemTypeFilter: ItemTypeFilter
    ): Flow<Result<List<Item>>> {
        TODO("Not yet implemented")
    }

    override suspend fun getById(userId: UserId, shareId: ShareId, itemId: ItemId): Result<Item> {
        TODO("Not yet implemented")
    }

    override suspend fun trashItem(userId: UserId, shareId: ShareId, itemId: ItemId): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun untrashItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun clearTrash(userId: UserId): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun restoreItems(userId: UserId): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun addPackageAndUrlToItem(
        shareId: ShareId,
        itemId: ItemId,
        packageName: Option<PackageName>,
        url: Option<String>
    ): Result<Item> {
        TODO("Not yet implemented")
    }

    override suspend fun refreshItems(userId: UserId, share: Share): Result<List<Item>> {
        TODO("Not yet implemented")
    }

    override suspend fun refreshItems(userId: UserId, shareId: ShareId): Result<List<Item>> {
        TODO("Not yet implemented")
    }

    override suspend fun applyEvents(
        userId: UserId,
        addressId: AddressId,
        shareId: ShareId,
        events: PendingEventList
    ) {
        TODO("Not yet implemented")
    }

    override fun observeItemCountSummary(userId: UserId, shareId: ShareId): Flow<ItemCountSummary> {
        TODO("Not yet implemented")
    }

    override suspend fun updateItemLastUsed(shareId: ShareId, itemId: ItemId) {
        TODO("Not yet implemented")
    }
}
