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
import proton.android.pass.data.impl.db.entities.ShareKeyEntity

@Dao
abstract class ShareKeysDao : BaseDao<ShareKeyEntity>() {
    @Query(
        """
        SELECT * FROM ${ShareKeyEntity.TABLE}
        WHERE ${ShareKeyEntity.Columns.USER_ID} = :userId
          AND ${ShareKeyEntity.Columns.SHARE_ID} = :shareId
        """
    )
    abstract fun getAllForShare(userId: String, shareId: String): Flow<List<ShareKeyEntity>>

    @Query(
        """
        SELECT * FROM ${ShareKeyEntity.TABLE}
        WHERE ${ShareKeyEntity.Columns.SHARE_ID} = :shareId
        ORDER BY ${ShareKeyEntity.Columns.ROTATION} DESC
        LIMIT 1
        """
    )
    abstract fun getLatestKeyForShare(shareId: String): Flow<ShareKeyEntity>

    @Query(
        """
        SELECT * FROM ${ShareKeyEntity.TABLE}
        WHERE ${ShareKeyEntity.Columns.USER_ID} = :userId
          AND ${ShareKeyEntity.Columns.SHARE_ID} = :shareId
          AND ${ShareKeyEntity.Columns.ROTATION} = :rotation
        LIMIT 1
        """
    )
    abstract fun getByShareAndRotation(
        userId: String,
        shareId: String,
        rotation: Long
    ): Flow<ShareKeyEntity?>
}
