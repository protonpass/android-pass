package me.proton.core.pass.data.remote

import me.proton.core.domain.entity.UserId
import me.proton.core.pass.data.crypto.CreateItemRequest
import me.proton.core.pass.data.crypto.UpdateItemRequest
import me.proton.core.pass.data.responses.ItemRevision
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ShareId

interface RemoteItemDataSource {
    suspend fun createItem(userId: UserId, shareId: ShareId, body: CreateItemRequest): ItemRevision
    suspend fun updateItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        body: UpdateItemRequest
    ): ItemRevision
    suspend fun getItems(userId: UserId, shareId: ShareId): List<ItemRevision>
    suspend fun delete(userId: UserId, shareId: ShareId, itemId: ItemId)
}
