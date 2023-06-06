package proton.android.pass.data.impl.remote

import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.ApiResult
import proton.android.pass.data.api.errors.CannotCreateMoreAliasesError
import proton.android.pass.data.api.errors.EmailNotValidatedError
import proton.android.pass.data.impl.api.PasswordManagerApi
import proton.android.pass.data.impl.remote.RemoteDataSourceConstants.PAGE_SIZE
import proton.android.pass.data.impl.requests.CreateAliasRequest
import proton.android.pass.data.impl.requests.CreateItemAliasRequest
import proton.android.pass.data.impl.requests.CreateItemRequest
import proton.android.pass.data.impl.requests.MigrateItemRequest
import proton.android.pass.data.impl.requests.MigrateItemsRequest
import proton.android.pass.data.impl.requests.TrashItemsRequest
import proton.android.pass.data.impl.requests.UpdateItemRequest
import proton.android.pass.data.impl.requests.UpdateLastUsedTimeRequest
import proton.android.pass.data.impl.responses.CreateItemAliasBundle
import proton.android.pass.data.impl.responses.ItemRevision
import proton.android.pass.data.impl.responses.TrashItemsResponse
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import javax.inject.Inject

const val CODE_CANNOT_CREATE_MORE_ALIASES = 300_007
const val CODE_USER_EMAIL_NOT_VALIDATED = 300_009

class RemoteItemDataSourceImpl @Inject constructor(
    private val api: ApiProvider
) : RemoteItemDataSource {

    override suspend fun createItem(
        userId: UserId,
        shareId: ShareId,
        body: CreateItemRequest
    ): ItemRevision =
        api.get<PasswordManagerApi>(userId)
            .invoke { createItem(shareId.id, body) }
            .valueOrThrow
            .item

    override suspend fun createAlias(
        userId: UserId,
        shareId: ShareId,
        body: CreateAliasRequest
    ): ItemRevision {
        val res = api.get<PasswordManagerApi>(userId)
            .invoke { createAlias(shareId.id, body) }
        when (res) {
            is ApiResult.Success -> return res.value.item
            is ApiResult.Error -> {
                if (res is ApiResult.Error.Http) {
                    when (res.proton?.code) {
                        CODE_CANNOT_CREATE_MORE_ALIASES -> throw CannotCreateMoreAliasesError()
                        CODE_USER_EMAIL_NOT_VALIDATED -> throw EmailNotValidatedError()
                        else -> {}
                    }
                }
                throw res.cause ?: Exception("Create alias failed")
            }
        }
    }

    override suspend fun createItemAndAlias(
        userId: UserId,
        shareId: ShareId,
        body: CreateItemAliasRequest
    ): CreateItemAliasBundle {
        val res = api.get<PasswordManagerApi>(userId)
            .invoke { createItemAndAlias(shareId.id, body) }
        when (res) {
            is ApiResult.Success -> return res.value.bundle
            is ApiResult.Error -> {
                if (res is ApiResult.Error.Http) {
                    when (res.proton?.code) {
                        CODE_CANNOT_CREATE_MORE_ALIASES -> throw CannotCreateMoreAliasesError()
                        CODE_USER_EMAIL_NOT_VALIDATED -> throw EmailNotValidatedError()
                        else -> {}
                    }
                }
                throw res.cause ?: Exception("Create item and alias failed")
            }
        }
    }

    override suspend fun updateItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        body: UpdateItemRequest
    ): ItemRevision =
        api.get<PasswordManagerApi>(userId)
            .invoke { updateItem(shareId.id, itemId.id, body) }
            .valueOrThrow
            .item

    override suspend fun getItems(
        userId: UserId,
        shareId: ShareId
    ): List<ItemRevision> =
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
            .valueOrThrow

    override suspend fun sendToTrash(
        userId: UserId,
        shareId: ShareId,
        body: TrashItemsRequest
    ): TrashItemsResponse =
        api.get<PasswordManagerApi>(userId)
            .invoke { trashItems(shareId.id, body) }
            .valueOrThrow

    override suspend fun untrash(
        userId: UserId,
        shareId: ShareId,
        body: TrashItemsRequest
    ): TrashItemsResponse =
        api.get<PasswordManagerApi>(userId)
            .invoke { untrashItems(shareId.id, body) }
            .valueOrThrow

    override suspend fun delete(
        userId: UserId,
        shareId: ShareId,
        body: TrashItemsRequest
    ) = api.get<PasswordManagerApi>(userId)
        .invoke { deleteItems(shareId.id, body) }
        .valueOrThrow

    override suspend fun updateLastUsedTime(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        now: Long
    ) {
        api.get<PasswordManagerApi>(userId)
            .invoke { updateLastUsedTime(shareId.id, itemId.id, UpdateLastUsedTimeRequest(now)) }
            .valueOrThrow
    }

    override suspend fun migrateItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        body: MigrateItemRequest
    ): ItemRevision =
        api.get<PasswordManagerApi>(userId)
            .invoke { migrateItem(shareId.id, itemId.id, body).item }
            .valueOrThrow

    override suspend fun migrateItems(
        userId: UserId,
        shareId: ShareId,
        body: MigrateItemsRequest
    ): List<ItemRevision> =
        api.get<PasswordManagerApi>(userId)
            .invoke { migrateItems(shareId.id, body).items }
            .valueOrThrow
}
