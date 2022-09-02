package me.proton.core.pass.data.remote

import javax.inject.Inject
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import me.proton.core.pass.data.api.PasswordManagerApi
import me.proton.core.pass.data.requests.CreateAliasRequest
import me.proton.core.pass.data.requests.CreateItemRequest
import me.proton.core.pass.data.requests.TrashItemsRequest
import me.proton.core.pass.data.requests.UpdateItemRequest
import me.proton.core.pass.data.responses.ItemRevision
import me.proton.core.pass.data.responses.TrashItemsResponse
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ShareId

class RemoteItemDataSourceImpl @Inject constructor(
    private val api: ApiProvider,
) : BaseRemoteDataSourceImpl(), RemoteItemDataSource {

    override suspend fun createItem(userId: UserId, shareId: ShareId, body: CreateItemRequest): ItemRevision =
        api.get<PasswordManagerApi>(userId).invoke {
            createItem(shareId.id, body)
        }.valueOrThrow.item

    override suspend fun createAlias(userId: UserId, shareId: ShareId, body: CreateAliasRequest): ItemRevision =
        api.get<PasswordManagerApi>(userId).invoke {
            createAlias(shareId.id, body)
        }.valueOrThrow.item

    override suspend fun updateItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        body: UpdateItemRequest
    ): ItemRevision =
        api.get<PasswordManagerApi>(userId).invoke {
            updateItem(shareId.id, itemId.id, body)
        }.valueOrThrow.item

    override suspend fun getItems(userId: UserId, shareId: ShareId): List<ItemRevision> =
        api.get<PasswordManagerApi>(userId).invoke {
            var page = 0
            val items = mutableListOf<ItemRevision>()
            while (true) {
                val pageItems = getItems(shareId.id, page, PAGE_SIZE)
                items.addAll(pageItems.items.revisions)
                if (pageItems.items.revisions.size < PAGE_SIZE) {
                    break
                } else {
                    page++
                }
            }
            items
        }.valueOrThrow

    override suspend fun sendToTrash(userId: UserId, shareId: ShareId, body: TrashItemsRequest): TrashItemsResponse =
        api.get<PasswordManagerApi>(userId).invoke {
            trashItems(shareId.id, body)
        }.valueOrThrow

    override suspend fun delete(userId: UserId, shareId: ShareId, body: TrashItemsRequest) =
        api.get<PasswordManagerApi>(userId).invoke {
            deleteItems(shareId.id, body)
        }.valueOrThrow
}
