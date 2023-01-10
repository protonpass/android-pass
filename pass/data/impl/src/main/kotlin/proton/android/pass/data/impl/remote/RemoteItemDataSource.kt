package proton.android.pass.data.impl.remote

import proton.android.pass.data.impl.requests.CreateAliasRequest
import proton.android.pass.data.impl.requests.CreateItemRequest
import proton.android.pass.data.impl.requests.TrashItemsRequest
import proton.android.pass.data.impl.requests.UpdateItemRequest
import proton.android.pass.data.impl.responses.ItemRevision
import proton.android.pass.data.impl.responses.TrashItemsResponse
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.Result
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

interface RemoteItemDataSource {
    suspend fun createItem(
        userId: UserId,
        shareId: ShareId,
        body: CreateItemRequest
    ): Result<ItemRevision>

    suspend fun createAlias(
        userId: UserId,
        shareId: ShareId,
        body: CreateAliasRequest
    ): Result<ItemRevision>

    suspend fun updateItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        body: UpdateItemRequest
    ): Result<ItemRevision>

    suspend fun getItems(userId: UserId, shareId: ShareId): Result<List<ItemRevision>>
    suspend fun sendToTrash(
        userId: UserId,
        shareId: ShareId,
        body: TrashItemsRequest
    ): Result<TrashItemsResponse>

    suspend fun untrash(
        userId: UserId,
        shareId: ShareId,
        body: TrashItemsRequest
    ): Result<TrashItemsResponse>

    suspend fun delete(userId: UserId, shareId: ShareId, body: TrashItemsRequest): Result<Unit>

    suspend fun updateLastUsedTime(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        now: Long
    ): Result<Unit>
}
