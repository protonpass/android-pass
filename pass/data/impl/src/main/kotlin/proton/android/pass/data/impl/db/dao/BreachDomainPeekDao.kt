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
import proton.android.pass.data.impl.db.entities.BreachDomainPeekEntity

@Dao
abstract class BreachDomainPeekDao : BaseDao<BreachDomainPeekEntity>() {
    @Query(
        """
        SELECT * FROM ${BreachDomainPeekEntity.TABLE}
        WHERE ${BreachDomainPeekEntity.Columns.USER_ID} = :userId
        ORDER BY ${BreachDomainPeekEntity.Columns.BREACH_TIME} DESC
        """
    )
    abstract fun observeByUserId(userId: String): Flow<List<BreachDomainPeekEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertAll(entities: List<BreachDomainPeekEntity>)

    @Query(
        """
        DELETE FROM ${BreachDomainPeekEntity.TABLE}
        WHERE ${BreachDomainPeekEntity.Columns.USER_ID} = :userId
        """
    )
    abstract suspend fun deleteAllForUser(userId: String)
}

