package me.proton.pass.data.remote

import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.data.requests.CreateAliasRequest
import me.proton.pass.data.requests.CreateItemRequest
import me.proton.pass.data.requests.TrashItemsRequest
import me.proton.pass.data.requests.UpdateItemRequest
import me.proton.pass.data.responses.ItemRevision
import me.proton.pass.data.responses.TrashItemsResponse
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ShareId

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
}
