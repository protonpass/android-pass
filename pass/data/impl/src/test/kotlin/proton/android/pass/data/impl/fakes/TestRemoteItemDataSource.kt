package proton.android.pass.data.impl.fakes

import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.data.impl.remote.RemoteItemDataSource
import proton.android.pass.data.impl.requests.CreateAliasRequest
import proton.android.pass.data.impl.requests.CreateItemAliasRequest
import proton.android.pass.data.impl.requests.CreateItemRequest
import proton.android.pass.data.impl.requests.MigrateItemRequest
import proton.android.pass.data.impl.requests.TrashItemsRequest
import proton.android.pass.data.impl.requests.UpdateItemRequest
import proton.android.pass.data.impl.responses.CreateItemAliasBundle
import proton.android.pass.data.impl.responses.ItemRevision
import proton.android.pass.data.impl.responses.TrashItemsResponse
import proton.android.pass.data.impl.util.TimeUtil
import proton.android.pass.test.crypto.TestKeyStoreCrypto
import proton.pass.domain.Item
import proton.pass.domain.ItemId
import proton.pass.domain.ItemStateValues
import proton.pass.domain.ShareId

class TestRemoteItemDataSource : RemoteItemDataSource {

    private var createItemResponse: () -> ItemRevision = { throw IllegalStateException("response not set") }
    private var createItemMemory: MutableList<CreateItemParams> = mutableListOf()

    fun getCreateItemMemory(): List<CreateItemParams> = createItemMemory

    fun setCreateItemResponse(delegate: () -> ItemRevision) {
        createItemResponse = delegate
    }

    override suspend fun createItem(
        userId: UserId,
        shareId: ShareId,
        body: CreateItemRequest
    ): ItemRevision {
        createItemMemory.add(CreateItemParams(userId, shareId, body))
        return createItemResponse()
    }

    override suspend fun createAlias(
        userId: UserId,
        shareId: ShareId,
        body: CreateAliasRequest
    ): ItemRevision {
        throw IllegalStateException("Not yet implemented")
    }

    override suspend fun createItemAndAlias(
        userId: UserId,
        shareId: ShareId,
        body: CreateItemAliasRequest
    ): CreateItemAliasBundle {
        throw IllegalStateException("Not yet implemented")
    }

    override suspend fun updateItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        body: UpdateItemRequest
    ): ItemRevision {
        throw IllegalStateException("Not yet implemented")
    }

    override suspend fun getItems(userId: UserId, shareId: ShareId): LoadingResult<List<ItemRevision>> {
        throw IllegalStateException("Not yet implemented")
    }

    override suspend fun sendToTrash(
        userId: UserId,
        shareId: ShareId,
        body: TrashItemsRequest
    ): LoadingResult<TrashItemsResponse> {
        throw IllegalStateException("Not yet implemented")
    }

    override suspend fun untrash(
        userId: UserId,
        shareId: ShareId,
        body: TrashItemsRequest
    ): LoadingResult<TrashItemsResponse> {
        throw IllegalStateException("Not yet implemented")
    }

    override suspend fun delete(
        userId: UserId,
        shareId: ShareId,
        body: TrashItemsRequest
    ): LoadingResult<Unit> {
        throw IllegalStateException("Not yet implemented")
    }

    override suspend fun updateLastUsedTime(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        now: Long
    ): LoadingResult<Unit> {
        throw IllegalStateException("Not yet implemented")
    }

    override suspend fun migrateItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        body: MigrateItemRequest
    ): ItemRevision {
        throw IllegalStateException("Not yet implemented")
    }

    data class CreateItemParams(
        val userId: UserId,
        val shareId: ShareId,
        val body: CreateItemRequest
    )

    companion object {
        fun createItemRevision(item: Item): ItemRevision {
            val now = TimeUtil.getNowUtc()
            return ItemRevision(
                itemId = item.id.id,
                revision = item.revision,
                contentFormatVersion = 1,
                keyRotation = 1,
                content = TestKeyStoreCrypto.encrypt("content"),
                state = ItemStateValues.ACTIVE,
                aliasEmail = null,
                createTime = now,
                modifyTime = now,
                lastUseTime = now,
                revisionTime = now,
                itemKey = null
            )
        }
    }
}
