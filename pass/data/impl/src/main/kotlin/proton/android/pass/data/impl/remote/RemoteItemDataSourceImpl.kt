/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.data.impl.remote

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.ApiResult
import proton.android.pass.data.api.errors.AliasRateLimitError
import proton.android.pass.data.api.errors.CannotCreateMoreAliasesError
import proton.android.pass.data.api.errors.EmailNotValidatedError
import proton.android.pass.data.api.errors.InvalidContentFormatVersionError
import proton.android.pass.data.api.errors.ItemNewerRevisionAvailable
import proton.android.pass.data.api.repositories.ItemRevision
import proton.android.pass.data.impl.api.PasswordManagerApi
import proton.android.pass.data.impl.extensions.toDomain
import proton.android.pass.data.impl.remote.RemoteDataSourceConstants.PAGE_SIZE
import proton.android.pass.data.impl.requests.CreateAliasRequest
import proton.android.pass.data.impl.requests.CreateItemAliasRequest
import proton.android.pass.data.impl.requests.CreateItemRequest
import proton.android.pass.data.impl.requests.MigrateItemsRequest
import proton.android.pass.data.impl.requests.TrashItemsRequest
import proton.android.pass.data.impl.requests.UpdateItemFlagsRequest
import proton.android.pass.data.impl.requests.UpdateItemRequest
import proton.android.pass.data.impl.requests.UpdateLastUsedTimeRequest
import proton.android.pass.data.impl.responses.CreateItemAliasBundle
import proton.android.pass.data.impl.responses.TrashItemsResponse
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import javax.inject.Inject

const val CODE_INVALID_CONTENT = 2001
const val CODE_CANNOT_CREATE_MORE_ALIASES = 300_007
const val CODE_USER_EMAIL_NOT_VALIDATED = 300_009
const val ALIAS_RATE_LIMIT = 2028
const val CODE_NOT_LATEST_REVISION = 300_002

class RemoteItemDataSourceImpl @Inject constructor(
    private val api: ApiProvider
) : RemoteItemDataSource {

    override suspend fun createItem(
        userId: UserId,
        shareId: ShareId,
        body: CreateItemRequest
    ): ItemRevision = api.get<PasswordManagerApi>(userId)
        .invoke { createItem(shareId.id, body) }
        .valueOrThrow
        .item
        .toDomain()

    override suspend fun createAlias(
        userId: UserId,
        shareId: ShareId,
        body: CreateAliasRequest
    ): ItemRevision {
        val res = api.get<PasswordManagerApi>(userId)
            .invoke { createAlias(shareId.id, body) }
        when (res) {
            is ApiResult.Success -> return res.value.item.toDomain()
            is ApiResult.Error -> {
                if (res is ApiResult.Error.Http) {
                    when (res.proton?.code) {
                        CODE_CANNOT_CREATE_MORE_ALIASES -> throw CannotCreateMoreAliasesError()
                        CODE_USER_EMAIL_NOT_VALIDATED -> throw EmailNotValidatedError()
                        ALIAS_RATE_LIMIT -> throw AliasRateLimitError()
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
    ): ItemRevision {
        val res = api.get<PasswordManagerApi>(userId)
            .invoke { updateItem(shareId.id, itemId.id, body) }
        when (res) {
            is ApiResult.Success -> return res.value.item.toDomain()
            is ApiResult.Error -> {
                if (res is ApiResult.Error.Http) {
                    when (res.proton?.code) {
                        CODE_INVALID_CONTENT -> throw InvalidContentFormatVersionError()
                        CODE_NOT_LATEST_REVISION -> throw ItemNewerRevisionAvailable()
                        else -> {}
                    }
                }
                throw res.cause ?: Exception("Update item failed")
            }
        }
    }

    override suspend fun updateItemFlags(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        body: UpdateItemFlagsRequest
    ): ItemRevision = api.get<PasswordManagerApi>(userId)
        .invoke { updateItemFlags(shareId.id, itemId.id, body) }
        .let { apiResult ->
            when (apiResult) {
                is ApiResult.Error -> throw apiResult.cause ?: Exception("Update item flags failed")
                is ApiResult.Success -> apiResult.value.item.toDomain()
            }
        }

    override suspend fun getItems(userId: UserId, shareId: ShareId): List<ItemRevision> =
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

                    val pageItems = response.items.revisions.toDomain()
                    items.addAll(pageItems)
                    if (pageItems.size < PAGE_SIZE || response.items.lastToken == null) {
                        break
                    }
                    sinceToken = response.items.lastToken
                }
                items
            }
            .valueOrThrow

    override fun observeItems(userId: UserId, shareId: ShareId): Flow<ItemTotal> = flow {
        api.get<PasswordManagerApi>(userId)
            .invoke {
                var sinceToken: String? = null
                var itemsRetrieved = 0

                while (true) {
                    val response = getItems(
                        shareId = shareId.id,
                        sinceToken = sinceToken,
                        pageSize = PAGE_SIZE
                    )

                    val total = response.items.total
                    val pageItems = response.items.revisions.toDomain()
                    itemsRetrieved += pageItems.size
                    emit(ItemTotal(total.toInt(), itemsRetrieved, pageItems))

                    if (pageItems.size < PAGE_SIZE || response.items.lastToken == null) {
                        break
                    }

                    sinceToken = response.items.lastToken
                }
            }
            .valueOrThrow
    }

    override suspend fun sendToTrash(
        userId: UserId,
        shareId: ShareId,
        body: TrashItemsRequest
    ): TrashItemsResponse = api.get<PasswordManagerApi>(userId)
        .invoke { trashItems(shareId.id, body) }
        .valueOrThrow

    override suspend fun untrash(
        userId: UserId,
        shareId: ShareId,
        body: TrashItemsRequest
    ): TrashItemsResponse = api.get<PasswordManagerApi>(userId)
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

    override suspend fun migrateItems(
        userId: UserId,
        shareId: ShareId,
        body: MigrateItemsRequest
    ): List<ItemRevision> = api.get<PasswordManagerApi>(userId)
        .invoke { migrateItems(shareId.id, body).items }
        .valueOrThrow
        .toDomain()

    override suspend fun pinItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): ItemRevision = api.get<PasswordManagerApi>(userId)
        .invoke { pinItem(shareId.id, itemId.id).item }
        .valueOrThrow
        .toDomain()

    override suspend fun unpinItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): ItemRevision = api.get<PasswordManagerApi>(userId)
        .invoke { unpinItem(shareId.id, itemId.id).item }
        .valueOrThrow
        .toDomain()

    override suspend fun fetchItemRevisions(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): List<ItemRevision> = api.get<PasswordManagerApi>(userId)
        .invoke { getItemRevision(shareId.id, itemId.id).itemsListApiModel.revisions }
        .valueOrThrow
        .toDomain()

}
