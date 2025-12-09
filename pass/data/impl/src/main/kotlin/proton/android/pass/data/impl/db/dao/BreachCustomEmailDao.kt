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
import proton.android.pass.data.impl.db.entities.BreachCustomEmailEntity

@Dao
abstract class BreachCustomEmailDao : BaseDao<BreachCustomEmailEntity>() {
    @Query(
        """
        SELECT * FROM ${BreachCustomEmailEntity.TABLE}
        WHERE ${BreachCustomEmailEntity.Columns.USER_ID} = :userId
        """
    )
    abstract fun observeByUserId(userId: String): Flow<List<BreachCustomEmailEntity>>

    @Query(
        """
        SELECT * FROM ${BreachCustomEmailEntity.TABLE}
        WHERE ${BreachCustomEmailEntity.Columns.USER_ID} = :userId
        AND ${BreachCustomEmailEntity.Columns.CUSTOM_EMAIL_ID} = :customEmailId
        LIMIT 1
        """
    )
    abstract fun observeByUserIdAndId(userId: String, customEmailId: String): Flow<BreachCustomEmailEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsert(entity: BreachCustomEmailEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertAll(entities: List<BreachCustomEmailEntity>)

    @Query(
        """
        DELETE FROM ${BreachCustomEmailEntity.TABLE}
        WHERE ${BreachCustomEmailEntity.Columns.USER_ID} = :userId
        AND ${BreachCustomEmailEntity.Columns.CUSTOM_EMAIL_ID} = :customEmailId
        """
    )
    abstract suspend fun delete(userId: String, customEmailId: String)

    @Query(
        """
        DELETE FROM ${BreachCustomEmailEntity.TABLE}
        WHERE ${BreachCustomEmailEntity.Columns.USER_ID} = :userId
        """
    )
    abstract suspend fun deleteAllForUser(userId: String)

    @Query(
        """
        SELECT * FROM ${BreachCustomEmailEntity.TABLE}
        """
    )
    abstract fun observeAll(): Flow<List<BreachCustomEmailEntity>>

    @Query(
        """
        SELECT SUM(${BreachCustomEmailEntity.Columns.BREACH_COUNT}) FROM ${BreachCustomEmailEntity.TABLE}
        WHERE ${BreachCustomEmailEntity.Columns.USER_ID} = :userId
        """
    )
    abstract fun observeTotalBreachCount(userId: String): Flow<Int?>
}

