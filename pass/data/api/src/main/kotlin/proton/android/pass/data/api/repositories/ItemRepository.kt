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

package proton.android.pass.data.api.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import proton.android.pass.common.api.Option
import proton.android.pass.data.api.ItemCountSummary
import proton.android.pass.data.api.ItemPendingEvent
import proton.android.pass.data.api.PendingEventList
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

data class ShareItemCount(
    val activeItems: Long,
    val trashedItems: Long
) {

    val totalItems: Long
        get() = activeItems.plus(trashedItems)

}

@Suppress("ComplexInterface", "TooManyFunctions")
interface ItemRepository {
    suspend fun createItem(
        userId: UserId,
        share: Share,
        contents: ItemContents
    ): Item

    suspend fun createAlias(
        userId: UserId,
        share: Share,
        newAlias: NewAlias
    ): Item

    suspend fun createItemAndAlias(
        userId: UserId,
        shareId: ShareId,
        contents: ItemContents,
        newAlias: NewAlias
    ): Item

    suspend fun updateItem(
        userId: UserId,
        share: Share,
        item: Item,
        contents: ItemContents
    ): Item

    suspend fun updateItemFlags(
        userId: UserId,
        share: Share,
        itemId: ItemId,
        flag: ItemFlag,
        isFlagEnabled: Boolean
    ): Item

    suspend fun updateLocalItemFlags(
        shareId: ShareId,
        itemId: ItemId,
        flag: ItemFlag,
        isFlagEnabled: Boolean
    )

    suspend fun updateLocalItemsFlags(
        items: List<Pair<ShareId, ItemId>>,
        flag: ItemFlag,
        isFlagEnabled: Boolean
    )

    fun observeItems(
        userId: UserId,
        shareSelection: ShareSelection,
        itemState: ItemState?,
        itemTypeFilter: ItemTypeFilter = ItemTypeFilter.All,
        setFlags: Int? = null,
        clearFlags: Int? = null
    ): Flow<List<Item>>

    fun observeEncryptedItems(
        userId: UserId,
        shareSelection: ShareSelection,
        itemState: ItemState?,
        itemTypeFilter: ItemTypeFilter = ItemTypeFilter.All,
        setFlags: Int? = null,
        clearFlags: Int? = null
    ): Flow<List<ItemEncrypted>>

    fun observePinnedItems(
        userId: UserId,
        shareSelection: ShareSelection,
        itemTypeFilter: ItemTypeFilter
    ): Flow<List<Item>>

    fun observeById(shareId: ShareId, itemId: ItemId): Flow<Item>

    suspend fun getById(shareId: ShareId, itemId: ItemId): Item
    suspend fun getByIds(shareId: ShareId, itemIds: List<ItemId>): List<Item>
    suspend fun trashItems(userId: UserId, items: Map<ShareId, List<ItemId>>)
    suspend fun untrashItems(userId: UserId, items: Map<ShareId, List<ItemId>>)
    suspend fun deleteItems(userId: UserId, items: Map<ShareId, List<ItemId>>)
    suspend fun clearTrash(userId: UserId)
    suspend fun restoreItems(userId: UserId)
    suspend fun addPackageAndUrlToItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        packageInfo: Option<PackageInfo>,
        url: Option<String>
    ): Item

    suspend fun refreshItems(userId: UserId, share: Share): List<Item>

    suspend fun refreshItems(userId: UserId, shareId: ShareId): List<Item>

    suspend fun downloadItemsAndObserveProgress(
        userId: UserId,
        shareId: ShareId,
        onProgress: suspend (VaultProgress) -> Unit
    ): List<ItemRevision>

    suspend fun applyEvents(
        userId: UserId,
        addressId: AddressId,
        shareId: ShareId,
        events: PendingEventList
    )

    suspend fun setShareItems(
        userId: UserId,
        items: Map<ShareId, List<ItemRevision>>,
        onProgress: suspend (VaultProgress) -> Unit
    )

    suspend fun applyPendingEvent(event: ItemPendingEvent)

    suspend fun purgePendingEvent(event: ItemPendingEvent): Boolean

    fun observeItemCountSummary(
        userId: UserId,
        shareIds: List<ShareId>,
        itemState: ItemState?
    ): Flow<ItemCountSummary>

    fun observeItemCount(shareIds: List<ShareId>): Flow<Map<ShareId, ShareItemCount>>

    suspend fun updateItemLastUsed(vaultId: VaultId, itemId: ItemId)

    suspend fun migrateItems(
        userId: UserId,
        items: Map<ShareId, List<ItemId>>,
        destination: Share
    ): MigrateItemsResult

    suspend fun migrateAllVaultItems(
        userId: UserId,
        source: ShareId,
        destination: ShareId
    )

    suspend fun getItemByAliasEmail(userId: UserId, aliasEmail: String): Item?

    suspend fun pinItems(items: List<Pair<ShareId, ItemId>>): PinItemsResult
    suspend fun unpinItems(items: List<Pair<ShareId, ItemId>>): PinItemsResult

    suspend fun getItemRevisions(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): List<ItemRevision>

    suspend fun addPasskeyToItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        passkey: Passkey
    )

    suspend fun findUserId(shareId: ShareId, itemId: ItemId): Option<UserId>

    suspend fun deleteItemRevisions(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    )

    fun observeSharedByMeEncryptedItems(userId: UserId, itemState: ItemState?): Flow<List<ItemEncrypted>>

    fun observeSharedWithMeEncryptedItems(userId: UserId, itemState: ItemState?): Flow<List<ItemEncrypted>>
}

data class VaultProgress(
    val total: Int,
    val current: Int
)

sealed interface MigrateItemsResult {
    @JvmInline
    value class AllMigrated(val items: List<Item>) : MigrateItemsResult

    @JvmInline
    value class SomeMigrated(val migratedItems: List<Item>) : MigrateItemsResult

    @JvmInline
    value class NoneMigrated(val exception: Throwable) : MigrateItemsResult
}

sealed interface PinItemsResult {
    @JvmInline
    value class AllPinned(val items: List<Item>) : PinItemsResult

    @JvmInline
    value class SomePinned(val migratedItems: List<Item>) : PinItemsResult

    @JvmInline
    value class NonePinned(val exception: Throwable) : PinItemsResult
}

@Serializable
data class ItemRevision(
    val itemId: String,
    val revision: Long,
    val contentFormatVersion: Int,
    val keyRotation: Long,
    val content: String,
    val itemKey: String?,
    val state: Int,
    val aliasEmail: String?,
    val createTime: Long,
    val modifyTime: Long,
    val lastUseTime: Long?,
    val revisionTime: Long,
    val isPinned: Boolean,
    val flags: Int,
    val shareCount: Int
)
