package me.proton.android.pass.data.impl.fakes

import me.proton.android.pass.data.impl.remote.RemoteItemDataSource
import me.proton.android.pass.data.impl.requests.CreateAliasRequest
import me.proton.android.pass.data.impl.requests.CreateItemRequest
import me.proton.android.pass.data.impl.requests.TrashItemsRequest
import me.proton.android.pass.data.impl.requests.UpdateItemRequest
import me.proton.android.pass.data.impl.responses.ItemRevision
import me.proton.android.pass.data.impl.responses.TrashItemsResponse
import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.domain.Item
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ItemStateValues
import me.proton.pass.domain.ShareId
import me.proton.pass.test.crypto.TestKeyStoreCrypto
import java.util.Date

class TestRemoteItemDataSource : RemoteItemDataSource {

    private var createItemResponse: () -> Result<ItemRevision> = { Result.Loading }
    private var createItemMemory: MutableList<CreateItemParams> = mutableListOf()

    fun getCreateItemMemory(): List<CreateItemParams> = createItemMemory

    fun setCreateItemResponse(delegate: () -> Result<ItemRevision>) {
        createItemResponse = delegate
    }

    override suspend fun createItem(
        userId: UserId,
        shareId: ShareId,
        body: CreateItemRequest
    ): Result<ItemRevision> {
        createItemMemory.add(CreateItemParams(userId, shareId, body))
        return createItemResponse()
    }

    override suspend fun createAlias(
        userId: UserId,
        shareId: ShareId,
        body: CreateAliasRequest
    ): Result<ItemRevision> {
        throw IllegalStateException("Not yet implemented")
    }

    override suspend fun updateItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        body: UpdateItemRequest
    ): Result<ItemRevision> {
        throw IllegalStateException("Not yet implemented")
    }

    override suspend fun getItems(userId: UserId, shareId: ShareId): Result<List<ItemRevision>> {
        throw IllegalStateException("Not yet implemented")
    }

    override suspend fun sendToTrash(
        userId: UserId,
        shareId: ShareId,
        body: TrashItemsRequest
    ): Result<TrashItemsResponse> {
        throw IllegalStateException("Not yet implemented")
    }

    override suspend fun untrash(
        userId: UserId,
        shareId: ShareId,
        body: TrashItemsRequest
    ): Result<TrashItemsResponse> {
        throw IllegalStateException("Not yet implemented")
    }

    override suspend fun delete(
        userId: UserId,
        shareId: ShareId,
        body: TrashItemsRequest
    ): Result<Unit> {
        throw IllegalStateException("Not yet implemented")
    }

    data class CreateItemParams(
        val userId: UserId,
        val shareId: ShareId,
        val body: CreateItemRequest
    )

    companion object {
        fun createItemRevision(item: Item): ItemRevision {
            val now = Date().time
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
                modifyTime = now
            )
        }
    }
}
