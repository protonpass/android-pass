package proton.android.pass.data.fakes.repositories

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.data.api.ItemCountSummary
import proton.android.pass.data.api.PendingEventList
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.usecases.ItemTypeFilter
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

    private val observeItemListFlow: MutableSharedFlow<LoadingResult<List<Item>>> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    fun sendObserveItemListResult(result: LoadingResult<List<Item>>) = observeItemListFlow.tryEmit(result)

    override suspend fun createItem(
        userId: UserId,
        share: Share,
        contents: ItemContents
    ): LoadingResult<Item> {
        TODO("Not yet implemented")
    }

    override suspend fun createAlias(
        userId: UserId,
        share: Share,
        newAlias: NewAlias
    ): LoadingResult<Item> {
        TODO("Not yet implemented")
    }

    override suspend fun updateItem(
        userId: UserId,
        share: Share,
        item: Item,
        contents: ItemContents
    ): LoadingResult<Item> {
        TODO("Not yet implemented")
    }

    override fun observeItems(
        userId: UserId,
        shareSelection: ShareSelection,
        itemState: ItemState,
        itemTypeFilter: ItemTypeFilter
    ): Flow<LoadingResult<List<Item>>> = observeItemListFlow

    override suspend fun getById(userId: UserId, shareId: ShareId, itemId: ItemId): LoadingResult<Item> {
        TODO("Not yet implemented")
    }

    override suspend fun trashItem(userId: UserId, shareId: ShareId, itemId: ItemId): LoadingResult<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun untrashItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): LoadingResult<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): LoadingResult<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun clearTrash(userId: UserId): LoadingResult<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun restoreItems(userId: UserId): LoadingResult<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun addPackageAndUrlToItem(
        shareId: ShareId,
        itemId: ItemId,
        packageName: Option<PackageName>,
        url: Option<String>
    ): LoadingResult<Item> {
        TODO("Not yet implemented")
    }

    override suspend fun refreshItems(userId: UserId, share: Share): LoadingResult<List<Item>> {
        TODO("Not yet implemented")
    }

    override suspend fun refreshItems(userId: UserId, shareId: ShareId): LoadingResult<List<Item>> {
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

    override fun observeItemCountSummary(
        userId: UserId,
        shareIds: List<ShareId>
    ): Flow<ItemCountSummary> {
        TODO("Not yet implemented")
    }

    override suspend fun updateItemLastUsed(shareId: ShareId, itemId: ItemId) {
        TODO("Not yet implemented")
    }
}
