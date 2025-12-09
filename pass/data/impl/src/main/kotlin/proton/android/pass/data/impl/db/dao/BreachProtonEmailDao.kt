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
import proton.android.pass.data.impl.db.entities.BreachProtonEmailEntity

@Dao
abstract class BreachProtonEmailDao : BaseDao<BreachProtonEmailEntity>() {
    @Query(
        """
        SELECT * FROM ${BreachProtonEmailEntity.TABLE}
        WHERE ${BreachProtonEmailEntity.Columns.USER_ID} = :userId
        """
    )
    abstract fun observeByUserId(userId: String): Flow<List<BreachProtonEmailEntity>>

    @Query(
        """
        SELECT * FROM ${BreachProtonEmailEntity.TABLE}
        WHERE ${BreachProtonEmailEntity.Columns.USER_ID} = :userId
        AND ${BreachProtonEmailEntity.Columns.ADDRESS_ID} = :addressId
        LIMIT 1
        """
    )
    abstract fun observeByUserIdAndId(userId: String, addressId: String): Flow<BreachProtonEmailEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsert(entity: BreachProtonEmailEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertAll(entities: List<BreachProtonEmailEntity>)

    @Query(
        """
        DELETE FROM ${BreachProtonEmailEntity.TABLE}
        WHERE ${BreachProtonEmailEntity.Columns.USER_ID} = :userId
        AND ${BreachProtonEmailEntity.Columns.ADDRESS_ID} = :addressId
        """
    )
    abstract suspend fun delete(userId: String, addressId: String)

    @Query(
        """
        DELETE FROM ${BreachProtonEmailEntity.TABLE}
        WHERE ${BreachProtonEmailEntity.Columns.USER_ID} = :userId
        """
    )
    abstract suspend fun deleteAllForUser(userId: String)

    @Query(
        """
        SELECT SUM(${BreachProtonEmailEntity.Columns.BREACH_COUNTER}) FROM ${BreachProtonEmailEntity.TABLE}
        WHERE ${BreachProtonEmailEntity.Columns.USER_ID} = :userId
        """
    )
    abstract fun observeTotalBreachCount(userId: String): Flow<Int?>
}

