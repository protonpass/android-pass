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

private const val SHARE_TYPE_VAULT = 1

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
        SELECT * FROM ${ShareEntity.TABLE} 
        WHERE ${ShareEntity.Columns.USER_ID} = :userId
          AND (:shareType IS NULL OR ${ShareEntity.Columns.SHARE_TYPE} = :shareType)
          AND (:isActive IS NULL OR ${ShareEntity.Columns.IS_ACTIVE} = :isActive)
        """
    )
    abstract fun observe(
        userId: String,
        shareType: Int? = null,
        isActive: Boolean? = null
    ): Flow<List<ShareEntity>>

    @Query(
        """
        SELECT ${ShareEntity.Columns.ID} FROM ${ShareEntity.TABLE} 
        WHERE ${ShareEntity.Columns.USER_ID} = :userId
          AND (:shareType IS NULL OR ${ShareEntity.Columns.SHARE_TYPE} = :shareType)
          AND (:shareRole IS NULL OR ${ShareEntity.Columns.SHARE_ROLE_ID} = :shareRole)
          AND (:isActive IS NULL OR ${ShareEntity.Columns.IS_ACTIVE} = :isActive)
    """
    )
    abstract fun observeSharedIds(
        userId: String,
        shareType: Int? = null,
        shareRole: String? = null,
        isActive: Boolean? = null
    ): Flow<List<String>>

    @Query(
        """
        SELECT COUNT(*) 
        FROM ${ShareEntity.TABLE}
        WHERE ${ShareEntity.Columns.USER_ID} = :userId
          AND ${ShareEntity.Columns.SHARE_TYPE} = $SHARE_TYPE_VAULT
          AND ${ShareEntity.Columns.IS_ACTIVE} = 1
        """
    )
    abstract fun observeActiveVaultCount(userId: String): Flow<Int>

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
