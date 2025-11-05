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

package proton.android.pass.data.impl.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.datetime.Instant
import me.proton.core.domain.entity.UserId
import me.proton.core.util.kotlin.takeIfNotEmpty
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.combineN
import proton.android.pass.common.api.toOption
import proton.android.pass.data.api.ItemCountSummary
import proton.android.pass.data.api.repositories.ShareItemCount
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.db.dao.SummaryRow
import proton.android.pass.data.impl.db.entities.ItemEntity
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.VaultId
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

@Suppress("TooManyFunctions")
class LocalItemDataSourceImpl @Inject constructor(
    private val database: PassDatabase,
    private val localShareDataSource: LocalShareDataSource
) : LocalItemDataSource {

    override suspend fun upsertItem(item: ItemEntity) = upsertItems(listOf(item))

    override suspend fun upsertItems(items: List<ItemEntity>) =
        database.itemsDao().insertOrUpdate(*items.toTypedArray())

    override fun observeItemsForShares(
        userId: UserId,
        shareIds: List<ShareId>,
        itemState: ItemState?,
        filter: ItemTypeFilter,
        setFlags: Int?,
        clearFlags: Int?
    ): Flow<List<ItemEntity>> = database.itemsDao().observeItems(
        userId = userId.id,
        shareIds = shareIds.map { it.id },
        itemState = itemState?.value,
        itemTypes = filter.value(),
        setFlags = setFlags,
        clearFlags = clearFlags
    )

    override fun observeItems(
        userId: UserId,
        itemState: ItemState?,
        filter: ItemTypeFilter,
        setFlags: Int?,
        clearFlags: Int?
    ): Flow<List<ItemEntity>> = database.itemsDao().observeItems(
        userId = userId.id,
        itemState = itemState?.value,
        itemTypes = filter.value(),
        setFlags = setFlags,
        clearFlags = clearFlags
    )

    override fun observePinnedItems(userId: UserId, filter: ItemTypeFilter): Flow<List<ItemEntity>> =
        database.itemsDao().observeItems(
            userId = userId.id,
            isPinned = true,
            itemTypes = filter.value()
        )

    override fun observeAllPinnedItemsForShares(
        userId: UserId,
        filter: ItemTypeFilter,
        shareIds: List<ShareId>
    ): Flow<List<ItemEntity>> = database.itemsDao().observeItems(
        userId = userId.id,
        isPinned = true,
        itemTypes = filter.value(),
        shareIds = shareIds.map { it.id }
    )

    override fun observeItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Flow<ItemEntity?> = database.itemsDao()
        .observeById(
            userId = userId.id,
            shareId = shareId.id,
            itemId = itemId.id
        )

    override suspend fun getById(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): ItemEntity? = database.itemsDao().observeById(
        userId = userId.id,
        shareId = shareId.id,
        itemId = itemId.id
    ).firstOrNull()

    override suspend fun getByIdList(
        userId: UserId,
        shareId: ShareId,
        itemIds: List<ItemId>
    ): List<ItemEntity> = database.itemsDao().observeItems(
        userId = userId.id,
        shareIds = listOf(shareId.id),
        itemIds = itemIds.map { it.id }
    ).firstOrNull()
        ?: emptyList()

    override suspend fun setItemStates(
        userId: UserId,
        shareId: ShareId,
        itemIds: List<ItemId>,
        itemState: ItemState
    ) = database.itemsDao().setItemState(
        userId = userId.id,
        shareId = shareId.id,
        itemIds = itemIds.map(ItemId::id),
        state = itemState.value
    )

    override suspend fun getTrashedItems(userId: UserId): List<ItemEntity> = database.itemsDao().observeItems(
        userId = userId.id,
        itemState = ItemState.Trashed.value
    ).firstOrNull()
        ?: emptyList()

    override suspend fun delete(
        userId: UserId,
        shareId: ShareId,
        itemIds: List<ItemId>
    ): Boolean {
        if (itemIds.isEmpty()) return true
        PassLogger.i(
            TAG, "Deleting items [shareId=${shareId.id}] [itemIds=${itemIds.map { it.id }}]"
        )
        return database.itemsDao().delete(
            userId = userId.id,
            shareId = shareId.id,
            itemIds = itemIds.map(ItemId::id)
        ) > 0
    }

    override suspend fun hasItemsForShare(userId: UserId, shareId: ShareId): Boolean =
        database.itemsDao().countItems(userId.id, listOf(shareId.id)) > 0

    override fun observeItemCountSummary(
        userId: UserId,
        shareIds: List<ShareId>,
        itemState: ItemState?,
        onlyShared: Boolean,
        applyItemStateToSharedItems: Boolean
    ): Flow<ItemCountSummary> = shareIds.map { shareId -> shareId.id }
        .takeIfNotEmpty()
        .let { shareIdValues ->
            combineN(
                observeItemSummary(
                    userId = userId,
                    itemState = itemState,
                    shareIds = shareIdValues,
                    onlyShared = onlyShared
                ),
                observeItemsWithTotpCount(
                    userId = userId,
                    itemState = itemState,
                    shareIds = shareIdValues
                ),
                observeSharedWithMeItemCount(
                    userId = userId,
                    shareIds = shareIdValues,
                    itemState = itemState,
                    applyItemStateToSharedItems = applyItemStateToSharedItems
                ),
                observeSharedByMeItemCount(
                    userId = userId,
                    shareIds = shareIdValues,
                    itemState = itemState,
                    applyItemStateToSharedItems = applyItemStateToSharedItems
                ),
                observeTrashedItemsCount(
                    userId = userId,
                    shareIds = shareIdValues
                ),
                observeSharedWithMeTrashedItemCount(userId = userId)
            ) { values: List<SummaryRow>,
                totpCount,
                sharedWithMeItemCount,
                sharedByMeItemCount,
                trashedItemsCount,
                sharedWithMeTrashedItemsCount ->

                ItemCountSummary(
                    login = values.getCount(ItemCategory.Login),
                    loginWithMFA = totpCount.toLong(),
                    note = values.getCount(ItemCategory.Note),
                    alias = values.getCount(ItemCategory.Alias),
                    creditCard = values.getCount(ItemCategory.CreditCard),
                    identities = values.getCount(ItemCategory.Identity),
                    custom = values.getCount(ItemCategory.Custom) +
                        values.getCount(ItemCategory.WifiNetwork) +
                        values.getCount(ItemCategory.SSHKey),
                    sharedWithMe = sharedWithMeItemCount.toLong(),
                    sharedByMe = sharedByMeItemCount.toLong(),
                    trashed = trashedItemsCount.toLong(),
                    sharedWithMeTrashed = sharedWithMeTrashedItemsCount.toLong()
                )
            }
        }

    private fun observeItemSummary(
        userId: UserId,
        itemState: ItemState?,
        shareIds: List<String>?,
        onlyShared: Boolean
    ): Flow<List<SummaryRow>> = database.itemsDao()
        .itemSummary(userId.id, itemState?.value, onlyShared)
        .map { summaryRows ->
            if (shareIds == null) summaryRows
            else summaryRows.filter { it.shareId in shareIds }
        }

    private fun List<SummaryRow>.getCount(itemCategory: ItemCategory): Long = filter {
        it.itemKind == itemCategory.value
    }.sumOf { it.itemCount }

    private fun observeItemsWithTotpCount(
        userId: UserId,
        itemState: ItemState?,
        shareIds: List<String>?
    ) = database.itemsDao().countItemsWithTotp(userId.id, itemState?.value)
        .map { rows ->
            rows.filter {
                if (shareIds == null) true
                else it.shareId in shareIds
            }.sumOf {
                it.itemCount
            }
        }

    private fun observeSharedWithMeItemCount(
        userId: UserId,
        shareIds: List<String>?,
        itemState: ItemState?,
        applyItemStateToSharedItems: Boolean
    ) = localShareDataSource.observeSharedWithMeIds(userId)
        .map { sharedWithMeShareIds ->
            sharedWithMeShareIds.filter { sharedWithMeShareId ->
                if (shareIds == null) true
                else sharedWithMeShareId in shareIds
            }
        }
        .flatMapLatest { sharedWithMeShareIds ->
            database.itemsDao().countSharedItems(
                userId = userId.id,
                shareIds = sharedWithMeShareIds,
                itemState = itemState?.value.takeIf { applyItemStateToSharedItems }
            )
        }

    private fun observeSharedByMeItemCount(
        userId: UserId,
        shareIds: List<String>?,
        itemState: ItemState?,
        applyItemStateToSharedItems: Boolean
    ) = localShareDataSource.observeSharedByMeIds(userId)
        .mapLatest { sharedByMeShareIds ->
            sharedByMeShareIds.filter { sharedByMeShareId ->
                if (shareIds == null) true
                else sharedByMeShareId in shareIds
            }
        }
        .flatMapLatest { sharedByMeShareIds ->
            database.itemsDao().countSharedItems(
                userId = userId.id,
                shareIds = sharedByMeShareIds,
                itemState = itemState?.value.takeIf { applyItemStateToSharedItems }
            )
        }

    private fun observeSharedWithMeTrashedItemCount(userId: UserId) =
        localShareDataSource.observeSharedWithMeIds(userId)
            .flatMapLatest { sharedWithMeShareIds ->
                observeTrashedItemsCount(userId, sharedWithMeShareIds)
            }

    private fun observeTrashedItemsCount(userId: UserId, shareIds: List<String>?) = database.itemsDao()
        .countTrashedItems(userId.id)
        .map { rows ->
            rows.filter {
                if (shareIds == null) true
                else it.shareId in shareIds
            }.sumOf {
                it.itemCount
            }
        }

    override suspend fun updateLastUsedTime(
        shareId: ShareId,
        itemId: ItemId,
        now: Long
    ) {
        database.itemsDao().updateLastUsedTime(shareId.id, itemId.id, now)
    }

    override fun observeItemCount(shareIds: List<ShareId>): Flow<Map<ShareId, ShareItemCount>> = database.itemsDao()
        .countItemsForShares(shareIds.map { it.id })
        .map { values ->
            shareIds.associate { shareId ->
                val rowForShare = values.firstOrNull { it.shareId == shareId.id }
                if (rowForShare == null) {
                    shareId to ShareItemCount(0, 0)
                } else {
                    shareId to ShareItemCount(
                        activeItems = rowForShare.activeItemCount,
                        trashedItems = rowForShare.trashedItemCount
                    )
                }
            }
        }

    override suspend fun getItemByAliasEmail(userId: UserId, aliasEmail: String): ItemEntity? =
        database.itemsDao().getItemByAliasEmail(userId.id, aliasEmail)

    override suspend fun getItemsPendingForTotpMigration(): List<ItemEntity> =
        database.itemsDao().getItemsPendingForTotpMigration()

    override suspend fun getItemsPendingForPasskeyMigration(): List<ItemEntity> =
        database.itemsDao().getItemsPendingForPasskeyMigration()

    override fun observeAllItemsWithTotp(userId: UserId): Flow<List<ItemWithTotp>> = database.itemsDao()
        .observeItems(
            userId = userId.id,
            hasTotp = true
        )
        .map { items -> items.map { it.toItemWithTotp() } }

    override fun countAllItemsWithTotp(userId: UserId): Flow<Int> = observeItemsWithTotpCount(
        userId = userId,
        itemState = null,
        shareIds = null
    )

    override fun observeItemsWithTotpForShare(userId: UserId, shareId: ShareId): Flow<List<ItemWithTotp>> =
        database.itemsDao()
            .observeItems(
                userId = userId.id,
                shareIds = listOf(shareId.id),
                hasTotp = true
            )
            .map { items -> items.map { it.toItemWithTotp() } }

    override fun observeAllItemsWithPasskeys(userId: UserId): Flow<List<ItemEntity>> = database.itemsDao().observeItems(
        userId = userId.id,
        hasPasskeys = true
    )

    override fun observeItemsWithPasskeys(userId: UserId, shareIds: List<ShareId>): Flow<List<ItemEntity>> =
        database.itemsDao().observeItems(
            userId = userId.id,
            hasPasskeys = true,
            shareIds = shareIds.map(ShareId::id)
        )

    override suspend fun updateItemFlags(
        shareId: ShareId,
        itemId: ItemId,
        flags: Int
    ) = database.itemsDao().updateItemFlags(shareId.id, itemId.id, flags)

    override fun getByVaultIdAndItemId(
        userIds: List<UserId>,
        vaultId: VaultId,
        itemId: ItemId
    ): List<ItemEntity> = database.itemsDao()
        .getByVaultIdAndItemId(
            userIds = userIds.map { it.id },
            vaultId = vaultId.id,
            itemId = itemId.id
        )

    override fun findUserId(shareId: ShareId, itemId: ItemId): Option<UserId> =
        database.itemsDao().findUserId(shareId.id, itemId.id)?.let(::UserId).toOption()

    private fun ItemEntity.toItemWithTotp(): ItemWithTotp = ItemWithTotp(
        shareId = ShareId(shareId),
        itemId = ItemId(id),
        createTime = Instant.fromEpochSeconds(createTime)
    )

    private fun ItemTypeFilter.value(): List<Int>? = when (this) {
        ItemTypeFilter.Logins -> listOf(ItemCategory.Login.value)
        ItemTypeFilter.Aliases -> listOf(ItemCategory.Alias.value)
        ItemTypeFilter.Notes -> listOf(ItemCategory.Note.value)
        ItemTypeFilter.CreditCards -> listOf(ItemCategory.CreditCard.value)
        ItemTypeFilter.Identity -> listOf(ItemCategory.Identity.value)
        ItemTypeFilter.Custom -> listOf(
            ItemCategory.Custom.value,
            ItemCategory.WifiNetwork.value,
            ItemCategory.SSHKey.value
        )

        ItemTypeFilter.All -> null
    }

    private companion object {

        private const val TAG = "LocalItemDataSourceImpl"

    }

}
