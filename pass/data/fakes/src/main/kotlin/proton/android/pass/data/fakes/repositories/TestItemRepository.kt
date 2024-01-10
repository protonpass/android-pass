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

package proton.android.pass.data.fakes.repositories

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import proton.android.pass.common.api.FlowUtils.testFlow
import proton.android.pass.common.api.Option
import proton.android.pass.data.api.ItemCountSummary
import proton.android.pass.data.api.PendingEventList
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.repositories.MigrateItemsResult
import proton.android.pass.data.api.repositories.ShareItemCount
import proton.android.pass.data.api.repositories.VaultProgress
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.Share
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareSelection
import proton.android.pass.domain.entity.NewAlias
import proton.android.pass.domain.entity.PackageInfo
import javax.inject.Inject

@Suppress("NotImplementedDeclaration", "TooManyFunctions")
class TestItemRepository @Inject constructor() : ItemRepository {

    private var item: Item? = null

    private var migrateItemResult: Result<MigrateItemsResult> =
        Result.failure(IllegalStateException("TestItemRepository.migrateItemResult not initialized"))
    private val observeItemListFlow: MutableSharedFlow<List<Item>> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private val itemFlow = testFlow<Item>()

    private val migrateItemMemory = mutableListOf<MigrateItemPayload>()

    suspend fun emitItem(newItem: Item) {
        itemFlow.emit(newItem)
    }

    fun setItem(newItem: Item) {
        item = newItem
    }

    override suspend fun createItem(
        userId: UserId,
        share: Share,
        contents: ItemContents
    ): Item {
        TODO("Not yet implemented")
    }

    override suspend fun createAlias(
        userId: UserId,
        share: Share,
        newAlias: NewAlias
    ): Item {
        TODO("Not yet implemented")
    }

    override suspend fun createItemAndAlias(
        userId: UserId,
        shareId: ShareId,
        contents: ItemContents,
        newAlias: NewAlias
    ): Item {
        TODO("Not yet implemented")
    }

    override suspend fun updateItem(
        userId: UserId,
        share: Share,
        item: Item,
        contents: ItemContents
    ): Item {
        TODO("Not yet implemented")
    }

    override fun observeItems(
        userId: UserId,
        shareSelection: ShareSelection,
        itemState: ItemState?,
        itemTypeFilter: ItemTypeFilter
    ): Flow<List<Item>> = observeItemListFlow

    override fun observeById(
        shareId: ShareId,
        itemId: ItemId,
    ): Flow<Item> = itemFlow

    override suspend fun getById(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Item {
        TODO("Not yet implemented")
    }

    override suspend fun trashItems(userId: UserId, items: Map<ShareId, List<ItemId>>) {
        TODO("Not yet implemented")
    }

    override suspend fun untrashItems(userId: UserId, items: Map<ShareId, List<ItemId>>) {
        TODO("Not yet implemented")
    }

    override suspend fun clearTrash(userId: UserId) {
        TODO("Not yet implemented")
    }

    override suspend fun restoreItems(userId: UserId) {
        TODO("Not yet implemented")
    }

    override suspend fun addPackageAndUrlToItem(
        shareId: ShareId,
        itemId: ItemId,
        packageInfo: Option<PackageInfo>,
        url: Option<String>
    ): Item {
        TODO("Not yet implemented")
    }

    override suspend fun refreshItems(userId: UserId, share: Share): List<Item> {
        TODO("Not yet implemented")
    }

    override suspend fun refreshItems(userId: UserId, shareId: ShareId): List<Item> {
        TODO("Not yet implemented")
    }

    override fun refreshItemsAndObserveProgress(
        userId: UserId,
        shareId: ShareId
    ): Flow<VaultProgress> {
        TODO("Not yet implemented")
    }

    override suspend fun applyEvents(
        userId: UserId,
        addressId: AddressId,
        shareId: ShareId,
        events: PendingEventList
    ) {
        TODO("Not yet implemented")
    }

    override fun observeItemCountSummary(
        userId: UserId,
        shareIds: List<ShareId>,
        itemState: ItemState?
    ): Flow<ItemCountSummary> {
        TODO("Not yet implemented")
    }

    override fun observeItemCount(shareIds: List<ShareId>): Flow<Map<ShareId, ShareItemCount>> {
        TODO("Not yet implemented")
    }

    override suspend fun updateItemLastUsed(shareId: ShareId, itemId: ItemId) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteItems(
        userId: UserId,
        items: Map<ShareId, List<ItemId>>
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun migrateItems(
        userId: UserId,
        items: Map<ShareId, List<ItemId>>,
        destination: Share
    ): MigrateItemsResult {
        migrateItemMemory.add(MigrateItemPayload(userId, items, destination))
        return migrateItemResult.getOrThrow()
    }


    override suspend fun migrateAllVaultItems(
        userId: UserId,
        source: ShareId,
        destination: ShareId
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun getItemByAliasEmail(userId: UserId, aliasEmail: String): Item? {
        TODO("Not yet implemented")
    }

    override suspend fun pinItem(
        shareId: ShareId,
        itemId: ItemId,
    ): Item = item?.copy(
        id = itemId,
        shareId = shareId,
        isPinned = true,
    ) ?: throw IllegalStateException("Item cannot be null. Did you forget to call setItem(item)?")

    override suspend fun unpinItem(
        shareId: ShareId,
        itemId: ItemId,
    ): Item = item?.copy(
        id = itemId,
        shareId = shareId,
        isPinned = false,
    ) ?: throw IllegalStateException("Item cannot be null. Did you forget to call setItem(item)?")

    data class MigrateItemPayload(
        val userId: UserId,
        val items: Map<ShareId, List<ItemId>>,
        val destination: Share
    )

}
