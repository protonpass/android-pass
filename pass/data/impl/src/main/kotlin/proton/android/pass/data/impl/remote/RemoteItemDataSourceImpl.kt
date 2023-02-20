package proton.android.pass.data.impl.remote

import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.ApiResult
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.map
import proton.android.pass.common.api.toLoadingResult
import proton.android.pass.data.api.errors.CannotCreateMoreAliasesError
import proton.android.pass.data.impl.api.PasswordManagerApi
import proton.android.pass.data.impl.remote.RemoteDataSourceConstants.PAGE_SIZE
import proton.android.pass.data.impl.requests.CreateAliasRequest
import proton.android.pass.data.impl.requests.CreateItemRequest
import proton.android.pass.data.impl.requests.TrashItemsRequest
import proton.android.pass.data.impl.requests.UpdateItemRequest
import proton.android.pass.data.impl.requests.UpdateLastUsedTimeRequest
import proton.android.pass.data.impl.responses.ItemRevision
import proton.android.pass.data.impl.responses.TrashItemsResponse
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import javax.inject.Inject

const val CODE_CANNOT_CREATE_MORE_ALIASES = 2011

class RemoteItemDataSourceImpl @Inject constructor(
    private val api: ApiProvider
) : RemoteItemDataSource {

    override suspend fun createItem(
        userId: UserId,
        shareId: ShareId,
        body: CreateItemRequest
    ): LoadingResult<ItemRevision> =
        api.get<PasswordManagerApi>(userId)
            .invoke { createItem(shareId.id, body) }
            .toLoadingResult()
            .map { it.item }

    override suspend fun createAlias(
        userId: UserId,
        shareId: ShareId,
        body: CreateAliasRequest
    ): LoadingResult<ItemRevision> {
        val res = api.get<PasswordManagerApi>(userId)
            .invoke { createAlias(shareId.id, body) }
        when (res) {
            is ApiResult.Success -> return LoadingResult.Success(res.value.item)
            is ApiResult.Error -> {
                if (res is ApiResult.Error.Http) {
                    if (res.proton?.code == CODE_CANNOT_CREATE_MORE_ALIASES) {
                        return LoadingResult.Error(CannotCreateMoreAliasesError())
                    }
                }
                return LoadingResult.Error(res.cause ?: Exception("Create alias failed"))
            }
        }
    }

    override suspend fun updateItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        body: UpdateItemRequest
    ): LoadingResult<ItemRevision> =
        api.get<PasswordManagerApi>(userId)
            .invoke { updateItem(shareId.id, itemId.id, body) }
            .toLoadingResult()
            .map { it.item }

    override suspend fun getItems(userId: UserId, shareId: ShareId): LoadingResult<List<ItemRevision>> =
        api.get<PasswordManagerApi>(userId)
            .invoke {
                var sinceToken: String? = null
                val items = mutableListOf<ItemRevision>()
                while (true) {
                    val response = getItems(
                        shareId = shareId.id,
                        sinceToken = sinceToken,
                        pageSize = PAGE_SIZE
                    )

                    val pageItems = response.items.revisions
                    items.addAll(pageItems)
                    if (pageItems.size < PAGE_SIZE || response.items.lastToken == null) {
                        break
                    }
                    sinceToken = response.items.lastToken
                }
                items
            }
            .toLoadingResult()

    override suspend fun sendToTrash(
        userId: UserId,
        shareId: ShareId,
        body: TrashItemsRequest
    ): LoadingResult<TrashItemsResponse> =
        api.get<PasswordManagerApi>(userId)
            .invoke { trashItems(shareId.id, body) }
            .toLoadingResult()

    override suspend fun untrash(
        userId: UserId,
        shareId: ShareId,
        body: TrashItemsRequest
    ): LoadingResult<TrashItemsResponse> =
        api.get<PasswordManagerApi>(userId)
            .invoke { untrashItems(shareId.id, body) }
            .toLoadingResult()

    override suspend fun delete(
        userId: UserId,
        shareId: ShareId,
        body: TrashItemsRequest
    ): LoadingResult<Unit> =
        api.get<PasswordManagerApi>(userId)
            .invoke { deleteItems(shareId.id, body) }
            .toLoadingResult()

    override suspend fun updateLastUsedTime(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        now: Long
    ): LoadingResult<Unit> =
        api.get<PasswordManagerApi>(userId)
            .invoke { updateLastUsedTime(shareId.id, itemId.id, UpdateLastUsedTimeRequest(now)) }
            .toLoadingResult()
            .map { }
}
