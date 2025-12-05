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
import proton.android.pass.common.api.FlowUtils.testFlow
import proton.android.pass.common.api.Option
import proton.android.pass.data.api.ItemCountSummary
import proton.android.pass.data.api.ItemPendingEvent
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.repositories.ItemRevision
import proton.android.pass.data.api.repositories.MigrateItemsResult
import proton.android.pass.data.api.repositories.PinItemsResult
import proton.android.pass.data.api.repositories.ShareItemCount
import proton.android.pass.data.api.repositories.VaultProgress
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemEncrypted
import proton.android.pass.domain.ItemFlag
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.Passkey
import proton.android.pass.domain.Share
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareSelection
import proton.android.pass.domain.VaultId
import proton.android.pass.domain.entity.NewAlias
import proton.android.pass.domain.entity.PackageInfo
import proton.android.pass.domain.events.EventToken
import javax.inject.Inject

@Suppress("NotImplementedDeclaration", "TooManyFunctions")
class FakeItemRepository @Inject constructor() : ItemRepository {

    private var item: Item? = null
    private var itemRevisions: List<ItemRevision>? = null

    private var migrateItemResult: Result<MigrateItemsResult> =
        Result.failure(IllegalStateException("TestItemRepository.migrateItemResult not initialized"))
    private val observeItemListFlow: MutableSharedFlow<List<Item>> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val observeItemEncryptedListFlow: MutableSharedFlow<List<ItemEncrypted>> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val deleteLocalItemsMemory: MutableList<DeleteLocalItemsPayload> = mutableListOf()
    private val refreshItemMemory: MutableList<RefreshItemPayload> = mutableListOf()

    private val encryptedSharedItemsFlow = testFlow<List<ItemEncrypted>>()

    private val itemFlow = testFlow<Item>()

    private val migrateItemMemory = mutableListOf<MigrateItemPayload>()

    fun setItem(newItem: Item) {
        item = newItem
    }

    fun setItemRevisions(newItemRevisions: List<ItemRevision>) {
        itemRevisions = newItemRevisions
    }

    fun emitValue(value: List<ItemEncrypted>) {
        encryptedSharedItemsFlow.tryEmit(value)
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

    override suspend fun createLoginAndAlias(
        userId: UserId,
        shareId: ShareId,
        contents: ItemContents.Login,
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

    override suspend fun updateItemFlags(
        userId: UserId,
        share: Share,
        itemId: ItemId,
        flag: ItemFlag,
        isFlagEnabled: Boolean
    ): Item {
        TODO("Not yet implemented")
    }

    override suspend fun updateLocalItemFlags(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        flag: ItemFlag,
        isFlagEnabled: Boolean
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun updateLocalItemsFlags(
        userId: UserId,
        items: List<Pair<ShareId, ItemId>>,
        flag: ItemFlag,
        isFlagEnabled: Boolean
    ) {
        TODO("Not yet implemented")
    }


    fun getDeleteLocalItemsMemory(): List<DeleteLocalItemsPayload> = deleteLocalItemsMemory.toList()

    fun clearDeleteLocalItemsMemory() {
        deleteLocalItemsMemory.clear()
    }

    override suspend fun deleteLocalItems(userId: UserId, items: Map<ShareId, List<ItemId>>) {
        deleteLocalItemsMemory.add(DeleteLocalItemsPayload(userId, items))
    }

    data class DeleteLocalItemsPayload(
        val userId: UserId,
        val items: Map<ShareId, List<ItemId>>
    )

    fun getRefreshItemMemory(): List<RefreshItemPayload> = refreshItemMemory.toList()

    fun clearRefreshItemMemory() {
        refreshItemMemory.clear()
    }

    override suspend fun refreshItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        eventToken: EventToken
    ) {
        refreshItemMemory.add(RefreshItemPayload(userId, shareId, itemId, eventToken))
    }

    data class RefreshItemPayload(
        val userId: UserId,
        val shareId: ShareId,
        val itemId: ItemId,
        val eventToken: EventToken
    )

    override fun observeItems(
        userId: UserId,
        shareSelection: ShareSelection,
        itemState: ItemState?,
        itemTypeFilter: ItemTypeFilter,
        itemFlags: Map<ItemFlag, Boolean>,
        includeHidden: Boolean
    ): Flow<List<Item>> = observeItemListFlow

    override fun observeEncryptedItems(
        userId: UserId,
        shareSelection: ShareSelection,
        itemState: ItemState?,
        itemTypeFilter: ItemTypeFilter,
        itemFlags: Map<ItemFlag, Boolean>,
        includeHidden: Boolean
    ): Flow<List<ItemEncrypted>> = observeItemEncryptedListFlow

    override fun observeSharedByMeEncryptedItems(
        userId: UserId,
        itemState: ItemState?,
        includeHiddenVault: Boolean
    ): Flow<List<ItemEncrypted>> = encryptedSharedItemsFlow

    override fun observeSharedWithMeEncryptedItems(
        userId: UserId,
        itemState: ItemState?,
        includeHiddenVault: Boolean
    ): Flow<List<ItemEncrypted>> = encryptedSharedItemsFlow

    override fun observePinnedItems(
        userId: UserId,
        shareSelection: ShareSelection,
        itemTypeFilter: ItemTypeFilter,
        includeHidden: Boolean
    ): Flow<List<Item>> {
        TODO("Not yet implemented")
    }

    override fun observeById(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Flow<Item> = itemFlow

    override suspend fun getById(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Item = item ?: throw IllegalStateException(
        "Item cannot be null, did you forget to invoke setItem()?"
    )

    override suspend fun getByIds(
        userId: UserId,
        shareId: ShareId,
        itemIds: List<ItemId>
    ): List<Item> = emptyList()

    override suspend fun trashItems(userId: UserId, items: Map<ShareId, List<ItemId>>) {
        TODO("Not yet implemented")
    }

    override suspend fun untrashItems(userId: UserId, items: Map<ShareId, List<ItemId>>) {
        TODO("Not yet implemented")
    }

    override suspend fun clearTrash(userId: UserId, includeHidden: Boolean) {
        TODO("Not yet implemented")
    }

    override suspend fun restoreItems(userId: UserId, includeHidden: Boolean) {
        TODO("Not yet implemented")
    }

    override suspend fun addPackageAndUrlToItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        packageInfo: Option<PackageInfo>,
        url: Option<String>
    ): Item {
        TODO("Not yet implemented")
    }

    override suspend fun downloadItemsAndObserveProgress(
        userId: UserId,
        shareId: ShareId,
        eventToken: EventToken?,
        onProgress: suspend (VaultProgress) -> Unit
    ): List<ItemRevision> {
        TODO("Not yet implemented")
    }

    override suspend fun setShareItems(
        userId: UserId,
        items: Map<ShareId, List<ItemRevision>>,
        onProgress: suspend (VaultProgress) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun applyPendingEvent(event: ItemPendingEvent) {}

    override suspend fun purgePendingEvent(event: ItemPendingEvent): Boolean = true

    override fun observeItemCountSummary(
        userId: UserId,
        shareIds: List<ShareId>,
        itemState: ItemState?,
        onlyShared: Boolean,
        applyItemStateToSharedItems: Boolean,
        includeHiddenVault: Boolean
    ): Flow<ItemCountSummary> = testFlow()

    override fun observeItemCount(shareIds: List<ShareId>): Flow<Map<ShareId, ShareItemCount>> {
        TODO("Not yet implemented")
    }

    override suspend fun updateItemLastUsed(vaultId: VaultId, itemId: ItemId) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteItems(userId: UserId, items: Map<ShareId, List<ItemId>>) {
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

    override suspend fun pinItems(items: List<Pair<ShareId, ItemId>>): PinItemsResult {
        TODO("Not yet implemented")
    }

    override suspend fun unpinItems(items: List<Pair<ShareId, ItemId>>): PinItemsResult {
        TODO("Not yet implemented")
    }

    override suspend fun getItemRevisions(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): List<ItemRevision> = itemRevisions ?: throw IllegalStateException(
        "Item revisions cannot be null, did you forget to invoke setItemRevisions()?"
    )

    override suspend fun addPasskeyToItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        passkey: Passkey
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun findUserId(shareId: ShareId, itemId: ItemId): Option<UserId> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteItemRevisions(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ) {
        TODO("Not yet implemented")
    }

    data class MigrateItemPayload(
        val userId: UserId,
        val items: Map<ShareId, List<ItemId>>,
        val destination: Share
    )

}
