/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.data.impl.repositories.fakes

import me.proton.core.domain.entity.UserId
import proton.android.pass.account.fakes.FakeKeyStoreCrypto
import proton.android.pass.data.api.repositories.ItemRevision
import proton.android.pass.data.impl.remote.ItemsPage
import proton.android.pass.data.impl.remote.RemoteItemDataSource
import proton.android.pass.data.impl.requests.CreateAliasRequest
import proton.android.pass.data.impl.requests.CreateItemAliasRequest
import proton.android.pass.data.impl.requests.CreateItemRequest
import proton.android.pass.data.impl.requests.MigrateItemsRequest
import proton.android.pass.data.impl.requests.TrashItemsRequest
import proton.android.pass.data.impl.requests.UpdateItemFlagsRequest
import proton.android.pass.data.impl.requests.UpdateItemRequest
import proton.android.pass.data.impl.responses.CreateItemAliasBundle
import proton.android.pass.data.impl.responses.TrashItemsResponse
import proton.android.pass.data.impl.util.TimeUtil
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemStateValues
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.events.EventToken
import proton.android.pass.test.domain.ItemTestFactory

class FakeRemoteItemDataSource : RemoteItemDataSource {

    private var createItemResponse: () -> ItemRevision =
        { throw IllegalStateException("response not set") }
    private var createItemMemory: MutableList<CreateItemParams> = mutableListOf()

    fun getCreateItemMemory(): List<CreateItemParams> = createItemMemory

    fun setCreateItemResponse(delegate: () -> ItemRevision) {
        createItemResponse = delegate
    }

    override suspend fun createItem(
        userId: UserId,
        shareId: ShareId,
        body: CreateItemRequest
    ): ItemRevision {
        createItemMemory.add(CreateItemParams(userId, shareId, body))
        return createItemResponse()
    }

    override suspend fun createAlias(
        userId: UserId,
        shareId: ShareId,
        body: CreateAliasRequest
    ): ItemRevision {
        throw IllegalStateException("Not yet implemented")
    }

    override suspend fun createItemAndAlias(
        userId: UserId,
        shareId: ShareId,
        body: CreateItemAliasRequest
    ): CreateItemAliasBundle {
        throw IllegalStateException("Not yet implemented")
    }

    override suspend fun updateItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        body: UpdateItemRequest
    ): ItemRevision {
        throw IllegalStateException("Not yet implemented")
    }

    override suspend fun updateItemFlags(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        body: UpdateItemFlagsRequest
    ): ItemRevision = ItemTestFactory.create(
        shareId = shareId,
        itemId = itemId
    ).let(::createItemRevision)

    override suspend fun getItems(userId: UserId, shareId: ShareId, eventToken: EventToken?): List<ItemRevision> {
        throw IllegalStateException("Not yet implemented")
    }

    override suspend fun getItemsPage(
        userId: UserId,
        shareId: ShareId,
        sinceToken: String?,
        eventToken: EventToken?
    ): ItemsPage {
        throw IllegalStateException("Not yet implemented")
    }

    override suspend fun getItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        eventToken: EventToken?
    ): ItemRevision {
        throw IllegalStateException("Not yet implemented")
    }

    override suspend fun sendToTrash(
        userId: UserId,
        shareId: ShareId,
        body: TrashItemsRequest
    ): TrashItemsResponse {
        throw IllegalStateException("Not yet implemented")
    }

    override suspend fun untrash(
        userId: UserId,
        shareId: ShareId,
        body: TrashItemsRequest
    ): TrashItemsResponse {
        throw IllegalStateException("Not yet implemented")
    }

    override suspend fun delete(
        userId: UserId,
        shareId: ShareId,
        body: TrashItemsRequest
    ) {
        throw IllegalStateException("Not yet implemented")
    }

    override suspend fun updateLastUsedTime(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        now: Long
    ) {
        throw IllegalStateException("Not yet implemented")
    }

    override suspend fun migrateItems(
        userId: UserId,
        shareId: ShareId,
        body: MigrateItemsRequest
    ): List<ItemRevision> {
        throw IllegalStateException("Not yet implemented")
    }

    override suspend fun pinItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): ItemRevision {
        throw IllegalStateException("Not yet implemented")
    }

    override suspend fun unpinItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): ItemRevision {
        throw IllegalStateException("Not yet implemented")
    }

    override suspend fun fetchItemRevisions(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): List<ItemRevision> = listOf(
        createItemRevision(
            ItemTestFactory.create(
                shareId = shareId,
                itemId = itemId
            )
        )
    )

    override suspend fun deleteItemRevisions(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): ItemRevision = createItemRevision(
        ItemTestFactory.create(
            shareId = shareId,
            itemId = itemId
        )
    )

    data class CreateItemParams(
        val userId: UserId,
        val shareId: ShareId,
        val body: CreateItemRequest
    )

    companion object {
        fun createItemRevision(item: Item): ItemRevision {
            val now = TimeUtil.getNowUtc()
            return ItemRevision(
                itemId = item.id.id,
                revision = item.revision,
                contentFormatVersion = 1,
                keyRotation = 1,
                content = FakeKeyStoreCrypto.encrypt("content"),
                state = ItemStateValues.ACTIVE,
                aliasEmail = null,
                createTime = now,
                modifyTime = now,
                lastUseTime = now,
                revisionTime = now,
                itemKey = null,
                isPinned = false,
                pinTime = now,
                flags = 0,
                shareCount = 0
            )
        }
    }
}

