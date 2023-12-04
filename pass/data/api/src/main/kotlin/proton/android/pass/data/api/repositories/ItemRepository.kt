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
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import proton.android.pass.common.api.Option
import proton.android.pass.data.api.ItemCountSummary
import proton.android.pass.data.api.PendingEventList
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

data class ShareItemCount(
    val activeItems: Long,
    val trashedItems: Long
)

@Suppress("ComplexInterface", "TooManyFunctions")
interface ItemRepository {
    suspend fun createItem(
        userId: UserId,
        share: Share,
        contents: ItemContents
    ): Item

    suspend fun createAlias(userId: UserId, share: Share, newAlias: NewAlias): Item
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

    fun observeItems(
        userId: UserId,
        shareSelection: ShareSelection,
        itemState: ItemState?,
        itemTypeFilter: ItemTypeFilter = ItemTypeFilter.All
    ): Flow<List<Item>>

    suspend fun getById(userId: UserId, shareId: ShareId, itemId: ItemId): Item
    suspend fun trashItems(userId: UserId, items: Map<ShareId, List<ItemId>>)
    suspend fun untrashItems(userId: UserId, items: Map<ShareId, List<ItemId>>)
    suspend fun deleteItems(userId: UserId, items: Map<ShareId, List<ItemId>>)
    suspend fun clearTrash(userId: UserId)
    suspend fun restoreItems(userId: UserId)
    suspend fun addPackageAndUrlToItem(
        shareId: ShareId,
        itemId: ItemId,
        packageInfo: Option<PackageInfo>,
        url: Option<String>
    ): Item

    suspend fun refreshItems(
        userId: UserId,
        share: Share
    ): List<Item>

    suspend fun refreshItems(
        userId: UserId,
        shareId: ShareId
    ): List<Item>

    fun refreshItemsAndObserveProgress(
        userId: UserId,
        shareId: ShareId
    ): Flow<VaultProgress>

    suspend fun applyEvents(
        userId: UserId,
        addressId: AddressId,
        shareId: ShareId,
        events: PendingEventList
    )

    fun observeItemCountSummary(
        userId: UserId,
        shareIds: List<ShareId>,
        itemState: ItemState?
    ): Flow<ItemCountSummary>

    fun observeItemCount(
        shareIds: List<ShareId>
    ): Flow<Map<ShareId, ShareItemCount>>

    suspend fun updateItemLastUsed(
        shareId: ShareId,
        itemId: ItemId
    )

    suspend fun migrateItem(
        userId: UserId,
        source: Share,
        destination: Share,
        itemId: ItemId
    ): Item

    suspend fun migrateItems(
        userId: UserId,
        source: ShareId,
        destination: ShareId,
    )

    suspend fun getItemByAliasEmail(
        userId: UserId,
        aliasEmail: String
    ): Item?
}

data class VaultProgress(
    val total: Int,
    val current: Int
)
