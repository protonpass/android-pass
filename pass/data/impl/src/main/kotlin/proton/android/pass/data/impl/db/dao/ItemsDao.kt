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

package proton.android.pass.data.impl.db.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.proton.core.data.room.db.BaseDao
import proton.android.pass.data.impl.db.entities.ItemEntity
import proton.android.pass.data.impl.db.entities.ShareEntity
import proton.android.pass.domain.ItemStateValues

data class SummaryRow(
    val itemKind: Int,
    val itemCount: Long
)

data class ShareItemCountRow(
    val shareId: String,
    val activeItemCount: Long,
    val trashedItemCount: Long
)

data class SharedItemsCountRow(
    val sharedWithMe: Long,
    val sharedByMe: Long
)

@Dao
@Suppress("TooManyFunctions")
abstract class ItemsDao : BaseDao<ItemEntity>() {
    @Query(
        """
        SELECT * FROM ${ItemEntity.TABLE} 
        WHERE ${ItemEntity.Columns.USER_ID} = :userId
          AND (${ItemEntity.Columns.STATE} = :itemState OR :itemState IS NULL)
          AND (:setFlags IS NULL OR (flags & :setFlags) == :setFlags)
          AND (:clearFlags IS NULL OR (flags & :clearFlags) == 0)
        ORDER BY ${ItemEntity.Columns.CREATE_TIME} DESC
        """
    )
    abstract fun observeAllForAddress(
        userId: String,
        itemState: Int?,
        setFlags: Int?,
        clearFlags: Int?
    ): Flow<List<ItemEntity>>

    @Query(
        """
        SELECT * FROM ${ItemEntity.TABLE} 
        WHERE ${ItemEntity.Columns.USER_ID} = :userId
          AND (${ItemEntity.Columns.STATE} = :itemState OR :itemState IS NULL)
          AND ${ItemEntity.Columns.ITEM_TYPE} = :itemType
          AND (:setFlags IS NULL OR (flags & :setFlags) == :setFlags)
          AND (:clearFlags IS NULL OR (flags & :clearFlags) == 0)
        ORDER BY ${ItemEntity.Columns.CREATE_TIME} DESC
        """
    )
    abstract fun observeAllForAddress(
        userId: String,
        itemState: Int?,
        itemType: Int,
        setFlags: Int?,
        clearFlags: Int?
    ): Flow<List<ItemEntity>>

    @Query(
        """
        SELECT * FROM ${ItemEntity.TABLE} 
        WHERE ${ItemEntity.Columns.USER_ID} = :userId
          AND ${ItemEntity.Columns.SHARE_ID} IN (:shareIds)
          AND (${ItemEntity.Columns.STATE} = :itemState OR :itemState IS NULL)
        ORDER BY ${ItemEntity.Columns.CREATE_TIME} DESC
        """
    )
    abstract fun observeAllForShares(
        userId: String,
        shareIds: List<String>,
        itemState: Int?
    ): Flow<List<ItemEntity>>

    @Query(
        """
        SELECT * FROM ${ItemEntity.TABLE} 
        WHERE ${ItemEntity.Columns.USER_ID} = :userId
          AND ${ItemEntity.Columns.SHARE_ID} IN (:shareIds)
          AND (${ItemEntity.Columns.STATE} = :itemState OR :itemState IS NULL)
          AND ${ItemEntity.Columns.ITEM_TYPE} = :itemType
        ORDER BY ${ItemEntity.Columns.CREATE_TIME} DESC
        """
    )
    abstract fun observeAllForShare(
        userId: String,
        shareIds: List<String>,
        itemState: Int?,
        itemType: Int
    ): Flow<List<ItemEntity>>

    @Query(
        """
        SELECT * FROM ${ItemEntity.TABLE} 
        WHERE ${ItemEntity.Columns.USER_ID} = :userId
          AND ${ItemEntity.Columns.IS_PINNED} = 1
          AND ${ItemEntity.Columns.ITEM_TYPE} = :itemType
        """
    )
    abstract fun observeAllPinnedItems(userId: String, itemType: Int): Flow<List<ItemEntity>>

    @Query(
        """
        SELECT * FROM ${ItemEntity.TABLE} 
        WHERE ${ItemEntity.Columns.USER_ID} = :userId
          AND ${ItemEntity.Columns.IS_PINNED} = 1
          AND ${ItemEntity.Columns.ITEM_TYPE} = :itemType
          AND ${ItemEntity.Columns.SHARE_ID} IN (:shareIds)
        """
    )
    abstract fun observeAllPinnedItemsForShares(
        userId: String,
        itemType: Int,
        shareIds: List<String>
    ): Flow<List<ItemEntity>>

    @Query(
        """
        SELECT * FROM ${ItemEntity.TABLE} 
        WHERE ${ItemEntity.Columns.USER_ID} = :userId
          AND ${ItemEntity.Columns.IS_PINNED} = 1
          AND ${ItemEntity.Columns.SHARE_ID} IN (:shareIds)
        """
    )
    abstract fun observeAllPinnedItemsForShares(userId: String, shareIds: List<String>): Flow<List<ItemEntity>>

    @Query(
        """
        SELECT * FROM ${ItemEntity.TABLE} 
        WHERE ${ItemEntity.Columns.USER_ID} = :userId
          AND ${ItemEntity.Columns.IS_PINNED} = 1
        """
    )
    abstract fun observeAllPinnedItems(userId: String): Flow<List<ItemEntity>>

    @Query(
        """
        SELECT * FROM ${ItemEntity.TABLE}
        WHERE ${ItemEntity.Columns.SHARE_ID} = :shareId
          AND ${ItemEntity.Columns.ID} = :itemId
        """
    )
    abstract fun observeById(shareId: String, itemId: String): Flow<ItemEntity>

    @Query(
        """
        SELECT * FROM ${ItemEntity.TABLE}
        WHERE ${ItemEntity.Columns.SHARE_ID} = :shareId
          AND ${ItemEntity.Columns.ID} = :itemId
        """
    )
    abstract suspend fun getById(shareId: String, itemId: String): ItemEntity?

    @Query(
        """
        SELECT * FROM ${ItemEntity.TABLE}
        WHERE ${ItemEntity.Columns.SHARE_ID} = :shareId
          AND ${ItemEntity.Columns.ID} IN (:itemIds)
        """
    )
    abstract suspend fun getByIdList(shareId: String, itemIds: List<String>): List<ItemEntity>

    @Query(
        """
        SELECT * FROM ${ItemEntity.TABLE}
        WHERE ${ItemEntity.Columns.USER_ID} = :userId
          AND ${ItemEntity.Columns.STATE} = :state
        ORDER BY ${ItemEntity.Columns.CREATE_TIME} DESC
        """
    )
    abstract suspend fun getItemsWithState(userId: String, state: Int): List<ItemEntity>

    @Query(
        """
        UPDATE ${ItemEntity.TABLE}
        SET ${ItemEntity.Columns.STATE} = :state
        WHERE ${ItemEntity.Columns.SHARE_ID} = :shareId
          AND ${ItemEntity.Columns.ID} = :itemId
        """
    )
    abstract suspend fun setItemState(
        shareId: String,
        itemId: String,
        state: Int
    )

    @Query(
        """
        UPDATE ${ItemEntity.TABLE}
        SET ${ItemEntity.Columns.STATE} = :state
        WHERE ${ItemEntity.Columns.SHARE_ID} = :shareId
          AND ${ItemEntity.Columns.ID} IN (:itemIds)
        """
    )
    abstract suspend fun setItemStates(
        shareId: String,
        itemIds: List<String>,
        state: Int
    )

    @Query(
        """
        DELETE FROM ${ItemEntity.TABLE} 
        WHERE ${ItemEntity.Columns.SHARE_ID} = :shareId
          AND ${ItemEntity.Columns.ID} = :itemId
        """
    )
    abstract suspend fun delete(shareId: String, itemId: String): Int

    @Query(
        """
        DELETE FROM ${ItemEntity.TABLE} 
        WHERE ${ItemEntity.Columns.SHARE_ID} = :shareId
          AND ${ItemEntity.Columns.ID} IN (:itemIds)
        """
    )
    abstract suspend fun deleteList(shareId: String, itemIds: List<String>): Int

    @Query(
        """
        SELECT COUNT(*)
        FROM ${ItemEntity.TABLE}
        WHERE ${ItemEntity.Columns.USER_ID} = :userId
          AND ${ItemEntity.Columns.SHARE_ID} = :shareId
        """
    )
    abstract suspend fun countItems(userId: String, shareId: String): Int

    @Query(
        """
        SELECT 
            ${ItemEntity.Columns.ITEM_TYPE} as itemKind,
            COUNT(${ItemEntity.Columns.ITEM_TYPE}) as itemCount
        FROM ${ItemEntity.TABLE}
        WHERE ${ItemEntity.Columns.USER_ID} = :userId
          AND ${ItemEntity.Columns.SHARE_ID} IN (:shareIds)
          AND (${ItemEntity.Columns.STATE} = :itemState OR :itemState IS NULL)
        GROUP BY ${ItemEntity.Columns.ITEM_TYPE}
        """
    )
    abstract fun itemSummary(
        userId: String,
        shareIds: List<String>,
        itemState: Int?
    ): Flow<List<SummaryRow>>

    @Query(
        """
        SELECT ${ItemEntity.Columns.SHARE_ID} as shareId,
        SUM(CASE WHEN ${ItemEntity.Columns.STATE} = ${ItemStateValues.ACTIVE} THEN 1 ELSE 0 END) as activeItemCount,
        SUM(CASE WHEN ${ItemEntity.Columns.STATE} = ${ItemStateValues.TRASHED} THEN 1 ELSE 0 END) as trashedItemCount
        FROM ${ItemEntity.TABLE}
        WHERE ${ItemEntity.Columns.SHARE_ID} IN (:shareIds)
        GROUP BY ${ItemEntity.Columns.SHARE_ID}
        """
    )
    abstract fun countItemsForShares(shareIds: List<String>): Flow<List<ShareItemCountRow>>

    @Query(
        """
        UPDATE ${ItemEntity.TABLE}
        SET ${ItemEntity.Columns.LAST_USED_TIME} = :now
        WHERE ${ItemEntity.Columns.ID} = :itemId
          AND ${ItemEntity.Columns.SHARE_ID} = :shareId
        """
    )
    abstract fun updateLastUsedTime(
        shareId: String,
        itemId: String,
        now: Long
    )

    @Query(
        """
        SELECT * FROM ${ItemEntity.TABLE}
        WHERE ${ItemEntity.Columns.USER_ID} = :userId
          AND ${ItemEntity.Columns.ALIAS_EMAIL} = :aliasEmail
        """
    )
    abstract suspend fun getItemByAliasEmail(userId: String, aliasEmail: String): ItemEntity?

    @Query(
        """
        SELECT * FROM ${ItemEntity.TABLE}
        WHERE ${ItemEntity.Columns.HAS_TOTP} IS NULL
        """
    )
    abstract suspend fun getItemsPendingForTotpMigration(): List<ItemEntity>

    @Query(
        """
        SELECT * FROM ${ItemEntity.TABLE}
        WHERE ${ItemEntity.Columns.HAS_PASSKEYS} IS NULL
        """
    )
    abstract suspend fun getItemsPendingForPasskeyMigration(): List<ItemEntity>

    @Query(
        """
        SELECT * FROM ${ItemEntity.TABLE}
        WHERE ${ItemEntity.Columns.USER_ID} = :userId
          AND ${ItemEntity.Columns.HAS_TOTP} = 1
        ORDER BY ${ItemEntity.Columns.CREATE_TIME} ASC
        """
    )
    abstract fun observeAllItemsWithTotp(userId: String): Flow<List<ItemEntity>>

    @Query(
        """
        SELECT COUNT(*) FROM ${ItemEntity.TABLE}
        WHERE ${ItemEntity.Columns.USER_ID} = :userId
          AND ${ItemEntity.Columns.HAS_TOTP} = 1
        """
    )
    abstract fun countAllItemsWithTotp(userId: String): Flow<Int>

    @Query(
        """
        SELECT * FROM ${ItemEntity.TABLE}
        WHERE ${ItemEntity.Columns.USER_ID} = :userId
          AND ${ItemEntity.Columns.HAS_PASSKEYS} = 1
          AND ${ItemEntity.Columns.STATE} = ${ItemStateValues.ACTIVE}
        ORDER BY ${ItemEntity.Columns.CREATE_TIME} ASC
        """
    )
    abstract fun observeAllItemsWithPasskeys(userId: String): Flow<List<ItemEntity>>

    @Query(
        """
        SELECT * FROM ${ItemEntity.TABLE}
        WHERE ${ItemEntity.Columns.USER_ID} = :userId
          AND ${ItemEntity.Columns.HAS_PASSKEYS} = 1
          AND ${ItemEntity.Columns.STATE} = ${ItemStateValues.ACTIVE}
          AND ${ItemEntity.Columns.SHARE_ID} IN (:shareIds)
        ORDER BY ${ItemEntity.Columns.CREATE_TIME} ASC
        """
    )
    abstract fun observeItemsWithPasskeys(userId: String, shareIds: List<String>): Flow<List<ItemEntity>>

    @Query(
        """
        SELECT * FROM ${ItemEntity.TABLE}
        WHERE ${ItemEntity.Columns.USER_ID} = :userId
          AND ${ItemEntity.Columns.SHARE_ID} = :shareId 
          AND ${ItemEntity.Columns.HAS_TOTP} = 1
        ORDER BY ${ItemEntity.Columns.CREATE_TIME} ASC
        """
    )
    abstract fun observeItemsWithTotpForShare(userId: String, shareId: String): Flow<List<ItemEntity>>

    @Query(
        """
        UPDATE ${ItemEntity.TABLE}
        SET ${ItemEntity.Columns.FLAGS} = :flags
        WHERE ${ItemEntity.Columns.SHARE_ID} = :shareId
            AND ${ItemEntity.Columns.ID} = :itemId
        """
    )
    abstract suspend fun updateItemFlags(
        shareId: String,
        itemId: String,
        flags: Int
    )

    @Query(
        """
        SELECT item.*, 
            share.${ShareEntity.Columns.ID} AS ${ItemEntity.Columns.SHARE_ID},
            share.${ShareEntity.Columns.USER_ID} AS share_user_id_alias
        FROM ${ItemEntity.TABLE} AS item
        JOIN ${ShareEntity.TABLE} AS share
        ON item.${ItemEntity.Columns.SHARE_ID} = share.${ShareEntity.Columns.ID}
        WHERE share.${ShareEntity.Columns.VAULT_ID} = :vaultId
          AND item.${ItemEntity.Columns.ID} = :itemId
          AND share_user_id_alias IN (:userIds)
        """
    )
    abstract fun getByVaultIdAndItemId(
        userIds: List<String>,
        vaultId: String,
        itemId: String
    ): List<ItemEntity>

    @Query(
        """
        SELECT ${ItemEntity.Columns.USER_ID} FROM ${ItemEntity.TABLE}
        WHERE ${ItemEntity.Columns.SHARE_ID} = :shareId
          AND ${ItemEntity.Columns.ID} = :itemId
        """
    )
    abstract fun findUserId(shareId: String, itemId: String): String?

    @Query(
        """
        SELECT 
            SUM(CASE WHEN ${ItemEntity.Columns.KEY} IS NULL THEN 1 ELSE 0 END) as sharedWithMe,
            SUM(CASE WHEN ${ItemEntity.Columns.KEY} IS NOT NULL THEN 1 ELSE 0 END) as sharedByMe
        FROM ${ItemEntity.TABLE}
        WHERE ${ItemEntity.Columns.USER_ID} = :userId
            AND ${ItemEntity.Columns.SHARE_COUNT} > 0
        """
    )
    abstract fun countSharedItems(userId: String): Flow<SharedItemsCountRow>
}
