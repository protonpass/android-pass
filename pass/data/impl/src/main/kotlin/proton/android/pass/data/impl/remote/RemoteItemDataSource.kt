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
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.ItemRevision
import proton.android.pass.data.impl.requests.CreateAliasRequest
import proton.android.pass.data.impl.requests.CreateItemAliasRequest
import proton.android.pass.data.impl.requests.CreateItemRequest
import proton.android.pass.data.impl.requests.MigrateItemsRequest
import proton.android.pass.data.impl.requests.TrashItemsRequest
import proton.android.pass.data.impl.requests.UpdateItemFlagsRequest
import proton.android.pass.data.impl.requests.UpdateItemRequest
import proton.android.pass.data.impl.responses.CreateItemAliasBundle
import proton.android.pass.data.impl.responses.TrashItemsResponse
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.events.EventToken

interface RemoteItemDataSource {

    suspend fun createItem(
        userId: UserId,
        shareId: ShareId,
        body: CreateItemRequest
    ): ItemRevision

    suspend fun createAlias(
        userId: UserId,
        shareId: ShareId,
        body: CreateAliasRequest
    ): ItemRevision

    suspend fun createItemAndAlias(
        userId: UserId,
        shareId: ShareId,
        body: CreateItemAliasRequest
    ): CreateItemAliasBundle

    suspend fun updateItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        body: UpdateItemRequest
    ): ItemRevision

    suspend fun updateItemFlags(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        body: UpdateItemFlagsRequest
    ): ItemRevision

    suspend fun getItems(
        userId: UserId,
        shareId: ShareId,
        eventToken: EventToken? = null
    ): List<ItemRevision>

    fun observeItems(
        userId: UserId,
        shareId: ShareId,
        eventToken: EventToken? = null
    ): Flow<ItemTotal>

    suspend fun sendToTrash(
        userId: UserId,
        shareId: ShareId,
        body: TrashItemsRequest
    ): TrashItemsResponse

    suspend fun untrash(
        userId: UserId,
        shareId: ShareId,
        body: TrashItemsRequest
    ): TrashItemsResponse

    suspend fun delete(
        userId: UserId,
        shareId: ShareId,
        body: TrashItemsRequest
    )

    suspend fun updateLastUsedTime(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        now: Long
    )

    suspend fun migrateItems(
        userId: UserId,
        shareId: ShareId,
        body: MigrateItemsRequest
    ): List<ItemRevision>

    suspend fun pinItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): ItemRevision

    suspend fun unpinItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): ItemRevision

    suspend fun fetchItemRevisions(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): List<ItemRevision>

    suspend fun deleteItemRevisions(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): ItemRevision
}

data class ItemTotal(
    val total: Int,
    val created: Int,
    val items: List<ItemRevision>
)
