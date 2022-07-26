package me.proton.core.pass.data.remote

import javax.inject.Inject
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import me.proton.core.pass.data.api.PasswordManagerApi
import me.proton.core.pass.data.crypto.CreateItemRequest
import me.proton.core.pass.data.responses.ItemRevision
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ShareId

class RemoteItemDataSourceImpl @Inject constructor(
    private val api: ApiProvider,
) : BaseRemoteDataSourceImpl(), RemoteItemDataSource {

    override suspend fun createItem(userId: UserId, shareId: ShareId, body: CreateItemRequest): ItemRevision =
        api.get<PasswordManagerApi>(userId).invoke {
            createItem(shareId.id, body)
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

    override suspend fun delete(userId: UserId, shareId: ShareId, itemId: ItemId) =
        api.get<PasswordManagerApi>(userId).invoke {
            deleteItem(shareId.id, itemId.id)
        }.valueOrThrow
}
