package proton.android.pass.data.impl.fakes

import proton.android.pass.data.impl.remote.RemoteItemDataSource
import proton.android.pass.data.impl.requests.CreateAliasRequest
import proton.android.pass.data.impl.requests.CreateItemRequest
import proton.android.pass.data.impl.requests.TrashItemsRequest
import proton.android.pass.data.impl.requests.UpdateItemRequest
import proton.android.pass.data.impl.responses.ItemRevision
import proton.android.pass.data.impl.responses.TrashItemsResponse
import proton.android.pass.data.impl.util.TimeUtil
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.LoadingResult
import proton.pass.domain.Item
import proton.pass.domain.ItemId
import proton.pass.domain.ItemStateValues
import proton.pass.domain.ShareId
import proton.android.pass.test.crypto.TestKeyStoreCrypto

class TestRemoteItemDataSource : RemoteItemDataSource {

    private var createItemResponse: () -> LoadingResult<ItemRevision> = { LoadingResult.Loading }
    private var createItemMemory: MutableList<CreateItemParams> = mutableListOf()

    fun getCreateItemMemory(): List<CreateItemParams> = createItemMemory

    fun setCreateItemResponse(delegate: () -> LoadingResult<ItemRevision>) {
        createItemResponse = delegate
    }

    override suspend fun createItem(
        userId: UserId,
        shareId: ShareId,
        body: CreateItemRequest
    ): LoadingResult<ItemRevision> {
        createItemMemory.add(CreateItemParams(userId, shareId, body))
        return createItemResponse()
    }

    override suspend fun createAlias(
        userId: UserId,
        shareId: ShareId,
        body: CreateAliasRequest
    ): LoadingResult<ItemRevision> {
        throw IllegalStateException("Not yet implemented")
    }

    override suspend fun updateItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        body: UpdateItemRequest
    ): LoadingResult<ItemRevision> {
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
                rotationId = "rotation",
                content = TestKeyStoreCrypto.encrypt("content"),
                userSignature = "userSignature",
                itemKeySignature = "itemKeySignature",
                state = ItemStateValues.ACTIVE,
                signatureEmail = "signatureEmail",
                aliasEmail = null,
                labels = emptyList(),
                createTime = now,
                modifyTime = now,
                lastUseTime = now,
                revisionTime = now
            )
        }
    }
}
