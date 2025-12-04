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
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.combineN
import proton.android.pass.common.api.toOption
import proton.android.pass.data.api.ItemCountSummary
import proton.android.pass.data.api.repositories.ShareItemCount
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.db.dao.SummaryRow
import proton.android.pass.data.impl.db.entities.ItemEntity
import proton.android.pass.domain.ItemFlag
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.VaultId
import proton.android.pass.domain.foldFlags
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

    override fun observeItems(
        userId: UserId,
        shareIds: List<ShareId>,
        itemState: ItemState?,
        filter: ItemTypeFilter,
        itemFlags: Map<ItemFlag, Boolean>
    ): Flow<List<ItemEntity>> {
        val (setFlags, clearFlags) = foldFlags(itemFlags)
        val itemTypes = filter.value()
        return database.itemsDao().observeItems(
            userId = userId.id,
            shareIds = shareIds.map(ShareId::id),
            itemIds = null,
            applyItemIds = false,
            itemTypes = itemTypes,
            applyItemTypes = itemTypes != null,
            itemState = itemState?.value,
            isPinned = null,
            hasTotp = null,
            hasPasskeys = null,
            setFlags = setFlags,
            clearFlags = clearFlags
        )
    }

    override fun observePinnedItems(
        userId: UserId,
        shareIds: List<ShareId>,
        filter: ItemTypeFilter
    ): Flow<List<ItemEntity>> {
        val itemTypes = filter.value()
        return database.itemsDao().observeItems(
            userId = userId.id,
            shareIds = shareIds.map(ShareId::id),
            itemIds = null,
            applyItemIds = false,
            itemTypes = itemTypes,
            applyItemTypes = itemTypes != null,
            itemState = null,
            isPinned = true,
            hasTotp = null,
            hasPasskeys = null,
            setFlags = null,
            clearFlags = null
        )
    }

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
        itemIds = itemIds.map { it.id },
        applyItemIds = true,
        itemTypes = null,
        applyItemTypes = false,
        itemState = null,
        isPinned = null,
        hasTotp = null,
        hasPasskeys = null,
        setFlags = null,
        clearFlags = null
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

    override suspend fun getTrashedItems(userId: UserId, shareIds: List<ShareId>): List<ItemEntity> =
        database.itemsDao().observeItems(
            userId = userId.id,
            shareIds = shareIds.map(ShareId::id),
            itemIds = null,
            applyItemIds = false,
            itemTypes = null,
            applyItemTypes = false,
            itemState = ItemState.Trashed.value,
            isPinned = null,
            hasTotp = null,
            hasPasskeys = null,
            setFlags = null,
            clearFlags = null
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
        applyItemStateToSharedItems: Boolean,
        includeHiddenVault: Boolean
    ): Flow<ItemCountSummary> = combineN(
        observeItemSummary(
            userId = userId,
            itemState = itemState,
            shareIds = shareIds,
            onlyShared = onlyShared
        ),
        observeItemsWithTotpCount(
            userId = userId,
            itemState = itemState,
            shareIds = shareIds
        ),
        observeSharedWithMeItemCount(
            userId = userId,
            shareIds = shareIds,
            itemState = itemState,
            applyItemStateToSharedItems = applyItemStateToSharedItems,
            includeHiddenVault = includeHiddenVault
        ),
        observeSharedByMeItemCount(
            userId = userId,
            shareIds = shareIds,
            itemState = itemState,
            applyItemStateToSharedItems = applyItemStateToSharedItems,
            includeHiddenVault = includeHiddenVault
        ),
        observeTrashedItemsCount(
            userId = userId,
            shareIds = shareIds
        ),
        observeSharedWithMeTrashedItemCount(
            userId = userId,
            shareIds = shareIds,
            includeHiddenVault = includeHiddenVault
        )
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

    private fun observeItemSummary(
        userId: UserId,
        shareIds: List<ShareId>,
        itemState: ItemState?,
        onlyShared: Boolean
    ): Flow<List<SummaryRow>> = database.itemsDao()
        .itemSummary(userId.id, shareIds.map { it.id }, itemState?.value, onlyShared)


    private fun List<SummaryRow>.getCount(itemCategory: ItemCategory): Long = filter {
        it.itemKind == itemCategory.value
    }.sumOf { it.itemCount }

    private fun observeItemsWithTotpCount(
        userId: UserId,
        shareIds: List<ShareId>,
        itemState: ItemState?
    ) = database.itemsDao().countItemsWithTotp(userId.id, shareIds.map { it.id }, itemState?.value)
        .map { rows -> rows.sumOf { it.itemCount } }

    private fun observeSharedWithMeItemCount(
        userId: UserId,
        shareIds: List<ShareId>,
        itemState: ItemState?,
        applyItemStateToSharedItems: Boolean,
        includeHiddenVault: Boolean
    ) = localShareDataSource.observeSharedWithMeIds(userId, includeHiddenVault)
        .map { sharedWithMeShareIds ->
            sharedWithMeShareIds.filter { sharedWithMeShareId ->
                sharedWithMeShareId.id in shareIds.map(ShareId::id)
            }
        }
        .flatMapLatest { sharedWithMeShareIds ->
            database.itemsDao().countSharedItems(
                userId = userId.id,
                shareIds = sharedWithMeShareIds.map(ShareId::id),
                itemState = itemState?.value.takeIf { applyItemStateToSharedItems }
            )
        }

    private fun observeSharedByMeItemCount(
        userId: UserId,
        shareIds: List<ShareId>,
        itemState: ItemState?,
        applyItemStateToSharedItems: Boolean,
        includeHiddenVault: Boolean
    ) = localShareDataSource.observeSharedByMeIds(userId, includeHiddenVault)
        .mapLatest { sharedByMeShareIds ->
            sharedByMeShareIds.filter { sharedByMeShareId ->
                sharedByMeShareId.id in shareIds.map(ShareId::id)
            }
        }
        .flatMapLatest { sharedByMeShareIds ->
            database.itemsDao().countSharedItems(
                userId = userId.id,
                shareIds = sharedByMeShareIds.map(ShareId::id),
                itemState = itemState?.value.takeIf { applyItemStateToSharedItems }
            )
        }

    private fun observeSharedWithMeTrashedItemCount(
        userId: UserId,
        shareIds: List<ShareId>,
        includeHiddenVault: Boolean
    ) = localShareDataSource.observeSharedWithMeIds(userId, includeHiddenVault)
        .mapLatest { sharedByMeShareIds ->
            sharedByMeShareIds.filter { sharedByMeShareId ->
                sharedByMeShareId.id in shareIds.map(ShareId::id)
            }
        }
        .flatMapLatest { sharedWithMeShareIds ->
            observeTrashedItemsCount(userId, sharedWithMeShareIds)
        }

    private fun observeTrashedItemsCount(userId: UserId, shareIds: List<ShareId>) = database.itemsDao()
        .countTrashedItems(userId.id, shareIds.map { it.id })
        .map { rows -> rows.sumOf { it.itemCount } }

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

    override fun countAllItemsWithTotp(userId: UserId, shareIds: List<ShareId>): Flow<Int> = observeItemsWithTotpCount(
        userId = userId,
        itemState = null,
        shareIds = shareIds
    )

    override fun observeItemsWithTotp(userId: UserId, shareIds: List<ShareId>): Flow<List<ItemWithTotp>> =
        database.itemsDao()
            .observeItems(
                userId = userId.id,
                shareIds = shareIds.map(ShareId::id),
                itemIds = null,
                applyItemIds = false,
                itemTypes = null,
                applyItemTypes = false,
                itemState = null,
                isPinned = null,
                hasTotp = true,
                hasPasskeys = null,
                setFlags = null,
                clearFlags = null
            )
            .map { items -> items.map { it.toItemWithTotp() } }

    override fun observeItemsWithPasskeys(userId: UserId, shareIds: List<ShareId>): Flow<List<ItemEntity>> =
        database.itemsDao().observeItems(
            userId = userId.id,
            shareIds = shareIds.map(ShareId::id),
            itemIds = null,
            applyItemIds = false,
            itemTypes = null,
            applyItemTypes = false,
            itemState = null,
            isPinned = null,
            hasTotp = null,
            hasPasskeys = true,
            setFlags = null,
            clearFlags = null
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
