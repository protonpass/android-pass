package me.proton.android.pass.data.impl.remote

import me.proton.android.pass.data.impl.requests.CreateAliasRequest
import me.proton.android.pass.data.impl.requests.CreateItemRequest
import me.proton.android.pass.data.impl.requests.TrashItemsRequest
import me.proton.android.pass.data.impl.requests.UpdateItemRequest
import me.proton.android.pass.data.impl.responses.ItemRevision
import me.proton.android.pass.data.impl.responses.TrashItemsResponse
import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
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

    suspend fun updateLastUsedTime(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        now: Long
    ): Result<Unit>
}
