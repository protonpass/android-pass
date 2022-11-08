package me.proton.pass.data.remote

import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.ApiResult
import me.proton.pass.common.api.Result
import me.proton.pass.common.api.map
import me.proton.pass.common.api.toResult
import me.proton.pass.data.api.PasswordManagerApi
import me.proton.pass.data.remote.RemoteDataSourceConstants.PAGE_SIZE
import me.proton.pass.data.requests.CreateAliasRequest
import me.proton.pass.data.requests.CreateItemRequest
import me.proton.pass.data.requests.TrashItemsRequest
import me.proton.pass.data.requests.UpdateItemRequest
import me.proton.pass.data.responses.ItemRevision
import me.proton.pass.data.responses.TrashItemsResponse
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ShareId
import me.proton.pass.domain.errors.CannotCreateMoreAliasesError
import javax.inject.Inject

const val CODE_CANNOT_CREATE_MORE_ALIASES = 2011

class RemoteItemDataSourceImpl @Inject constructor(
    private val api: ApiProvider
) : RemoteItemDataSource {

    override suspend fun createItem(
        userId: UserId,
        shareId: ShareId,
        body: CreateItemRequest
    ): Result<ItemRevision> =
        api.get<PasswordManagerApi>(userId)
            .invoke { createItem(shareId.id, body) }
            .toResult()
            .map { it.item }

    override suspend fun createAlias(
        userId: UserId,
        shareId: ShareId,
        body: CreateAliasRequest
    ): Result<ItemRevision> {
        val res = api.get<PasswordManagerApi>(userId).invoke { createAlias(shareId.id, body) }
        when (res) {
            is ApiResult.Success -> return Result.Success(res.value.item)
            is ApiResult.Error -> {
                if (res is ApiResult.Error.Http) {
                    if (res.proton?.code == CODE_CANNOT_CREATE_MORE_ALIASES) {
                        return Result.Error(CannotCreateMoreAliasesError())
                    }
                }
                return Result.Error(res.cause)
            }
        }
    }

    override suspend fun updateItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        body: UpdateItemRequest
    ): Result<ItemRevision> =
        api.get<PasswordManagerApi>(userId)
            .invoke { updateItem(shareId.id, itemId.id, body) }
            .toResult()
            .map { it.item }

    override suspend fun getItems(userId: UserId, shareId: ShareId): Result<List<ItemRevision>> =
        api.get<PasswordManagerApi>(userId)
            .invoke {
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
            }
            .toResult()

    override suspend fun sendToTrash(
        userId: UserId,
        shareId: ShareId,
        body: TrashItemsRequest
    ): Result<TrashItemsResponse> =
        api.get<PasswordManagerApi>(userId)
            .invoke { trashItems(shareId.id, body) }
            .toResult()

    override suspend fun untrash(
        userId: UserId,
        shareId: ShareId,
        body: TrashItemsRequest
    ): Result<TrashItemsResponse> =
        api.get<PasswordManagerApi>(userId)
            .invoke { untrashItems(shareId.id, body) }
            .toResult()

    override suspend fun delete(
        userId: UserId,
        shareId: ShareId,
        body: TrashItemsRequest
    ): Result<Unit> =
        api.get<PasswordManagerApi>(userId)
            .invoke { deleteItems(shareId.id, body) }
            .toResult()
}
