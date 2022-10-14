package me.proton.core.pass.data.remote

import me.proton.core.domain.entity.UserId
import me.proton.core.pass.common.api.Result
import me.proton.core.pass.data.requests.CreateAliasRequest
import me.proton.core.pass.data.requests.CreateItemRequest
import me.proton.core.pass.data.requests.TrashItemsRequest
import me.proton.core.pass.data.requests.UpdateItemRequest
import me.proton.core.pass.data.responses.ItemRevision
import me.proton.core.pass.data.responses.TrashItemsResponse
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ShareId

interface RemoteItemDataSource {
    suspend fun createItem(userId: UserId, shareId: ShareId, body: CreateItemRequest): ItemRevision
    suspend fun createAlias(userId: UserId, shareId: ShareId, body: CreateAliasRequest): ItemRevision
    suspend fun updateItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        body: UpdateItemRequest
    ): Result<ItemRevision>
    suspend fun getItems(userId: UserId, shareId: ShareId): List<ItemRevision>
    suspend fun sendToTrash(userId: UserId, shareId: ShareId, body: TrashItemsRequest): TrashItemsResponse
    suspend fun untrash(userId: UserId, shareId: ShareId, body: TrashItemsRequest): TrashItemsResponse
    suspend fun delete(userId: UserId, shareId: ShareId, body: TrashItemsRequest)
}
