/*
 * Copyright (c) 2024 Proton AG
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
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.proton.core.data.room.db.BaseDao
import proton.android.pass.data.impl.db.entities.BreachEmailEntity

@Dao
abstract class BreachEmailDao : BaseDao<BreachEmailEntity>() {
    @Query(
        """
        SELECT * FROM ${BreachEmailEntity.TABLE}
        WHERE ${BreachEmailEntity.Columns.USER_ID} = :userId
        AND ${BreachEmailEntity.Columns.EMAIL_TYPE} = :emailType
        AND ${BreachEmailEntity.Columns.EMAIL_OWNER_ID} = :emailOwnerId
        ORDER BY ${BreachEmailEntity.Columns.PUBLISHED_AT} DESC
        """
    )
    abstract fun observeByOwner(
        userId: String,
        emailType: Int,
        emailOwnerId: String
    ): Flow<List<BreachEmailEntity>>

    @Query(
        """
        SELECT * FROM ${BreachEmailEntity.TABLE}
        WHERE ${BreachEmailEntity.Columns.USER_ID} = :userId
        AND ${BreachEmailEntity.Columns.EMAIL_TYPE} = ${BreachEmailEntity.EMAIL_TYPE_ALIAS}
        AND ${BreachEmailEntity.Columns.SHARE_ID} = :shareId
        AND ${BreachEmailEntity.Columns.ITEM_ID} = :itemId
        ORDER BY ${BreachEmailEntity.Columns.PUBLISHED_AT} DESC
        """
    )
    abstract fun observeByAlias(
        userId: String,
        shareId: String,
        itemId: String
    ): Flow<List<BreachEmailEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertAll(entities: List<BreachEmailEntity>)

    @Query(
        """
        DELETE FROM ${BreachEmailEntity.TABLE}
        WHERE ${BreachEmailEntity.Columns.USER_ID} = :userId
        AND ${BreachEmailEntity.Columns.EMAIL_TYPE} = :emailType
        AND ${BreachEmailEntity.Columns.EMAIL_OWNER_ID} = :emailOwnerId
        """
    )
    abstract suspend fun deleteByOwner(
        userId: String,
        emailType: Int,
        emailOwnerId: String
    )

    @Query(
        """
        DELETE FROM ${BreachEmailEntity.TABLE}
        WHERE ${BreachEmailEntity.Columns.USER_ID} = :userId
        AND ${BreachEmailEntity.Columns.EMAIL_TYPE} = ${BreachEmailEntity.EMAIL_TYPE_ALIAS}
        AND ${BreachEmailEntity.Columns.SHARE_ID} = :shareId
        AND ${BreachEmailEntity.Columns.ITEM_ID} = :itemId
        """
    )
    abstract suspend fun deleteByAlias(
        userId: String,
        shareId: String,
        itemId: String
    )
}

