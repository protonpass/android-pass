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
import proton.android.pass.data.impl.db.entities.ShareEntity
import proton.android.pass.data.impl.db.entities.ShareKeyView

@Dao
abstract class SharesDao : BaseDao<ShareEntity>() {

    @Query(
        """
        SELECT * FROM ${ShareEntity.TABLE}
        WHERE ${ShareEntity.Columns.USER_ID} = :userId
          AND ${ShareEntity.Columns.ID} = :shareId
        LIMIT 1
        """
    )
    abstract fun observeById(userId: String, shareId: String): Flow<ShareEntity?>

    @Query(
        """
        SELECT ${ShareEntity.Columns.ID} FROM ${ShareEntity.TABLE} 
        WHERE ${ShareEntity.Columns.USER_ID} = :userId
          AND ${ShareEntity.Columns.ID} IN (:shareIds)
          AND (:shareType IS NULL OR ${ShareEntity.Columns.SHARE_TYPE} = :shareType)
          AND (:shareRole IS NULL OR ${ShareEntity.Columns.SHARE_ROLE_ID} = :shareRole)
          AND ${ShareEntity.Columns.IS_ACTIVE} = 1
        """
    )
    abstract fun observeIds(
        userId: String,
        shareIds: List<String>,
        shareType: Int? = null,
        shareRole: String? = null
    ): Flow<List<String>>

    @Query(
        """
        SELECT * FROM ${ShareEntity.TABLE} 
        WHERE ${ShareEntity.Columns.USER_ID} = :userId
        """
    )
    abstract fun observeAllIncludingInactive(userId: String): Flow<List<ShareEntity>>

    @Query(
        """
        SELECT * FROM ${ShareEntity.TABLE} 
        WHERE ${ShareEntity.Columns.USER_ID} = :userId
          AND ${ShareEntity.Columns.ID} IN (:shareIds)
          AND (:shareType IS NULL OR ${ShareEntity.Columns.SHARE_TYPE} = :shareType)
          AND (:shareRole IS NULL OR ${ShareEntity.Columns.SHARE_ROLE_ID} = :shareRole)
          AND ${ShareEntity.Columns.IS_ACTIVE} = 1
        """
    )
    abstract fun observeActive(
        userId: String,
        shareIds: List<String>,
        shareType: Int? = null,
        shareRole: String? = null
    ): Flow<List<ShareEntity>>

    @Query(
        """
        SELECT COUNT(*) FROM ${ShareEntity.TABLE} 
        WHERE ${ShareEntity.Columns.USER_ID} = :userId
          AND ${ShareEntity.Columns.ID} IN (:shareIds)
          AND (:shareType IS NULL OR ${ShareEntity.Columns.SHARE_TYPE} = :shareType)
          AND ${ShareEntity.Columns.IS_ACTIVE} = 1
        """
    )
    abstract fun observeCount(
        userId: String,
        shareIds: List<String>,
        shareType: Int? = null
    ): Flow<Int>

    @Query(
        """
        SELECT
            ${ShareEntity.Columns.ID},
            ${ShareEntity.Columns.VAULT_ID},
            ${ShareEntity.Columns.SHARE_TYPE},
            ${ShareEntity.Columns.TARGET_ID},
            ${ShareEntity.Columns.SHARE_ROLE_ID},
            ${ShareEntity.Columns.PERMISSION}
        FROM ${ShareEntity.TABLE}
        WHERE ${ShareEntity.Columns.USER_ID} = :userId
          AND ${ShareEntity.Columns.IS_ACTIVE} = 1
        """
    )
    abstract fun observeShareKeyView(userId: String): Flow<List<ShareKeyView>>

    @Query(
        """
        DELETE FROM ${ShareEntity.TABLE}
        WHERE ${ShareEntity.Columns.USER_ID} = :userId
          AND (:shareIds IS NULL OR ${ShareEntity.Columns.ID} IN (:shareIds))
        """
    )
    abstract suspend fun deleteShares(userId: String, shareIds: List<String>? = null): Int

    @Query(
        """
        UPDATE ${ShareEntity.TABLE}
        SET ${ShareEntity.Columns.OWNER} = :isOwner
        WHERE ${ShareEntity.Columns.USER_ID} = :userId
          AND ${ShareEntity.Columns.ID} = :shareId
        """
    )
    abstract suspend fun updateOwnership(
        userId: String,
        shareId: String,
        isOwner: Boolean
    )

    @Query(
        """
        SELECT EXISTS(
            SELECT 1
            FROM ${ShareEntity.TABLE}
            WHERE ${ShareEntity.Columns.ID} = :shareId
                AND ${ShareEntity.Columns.USER_ID} = :userId
        )
        """
    )
    abstract suspend fun checkIfShareExists(userId: String, shareId: String): Boolean
}
