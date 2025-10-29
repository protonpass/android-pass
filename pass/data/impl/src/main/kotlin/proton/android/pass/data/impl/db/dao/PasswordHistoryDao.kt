/*
 * Copyright (c) 2025 Proton AG
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
import proton.android.pass.data.impl.db.entities.PasswordHistoryEntity

@Dao
abstract class PasswordHistoryDao : BaseDao<PasswordHistoryEntity>() {
    @Query(
        """
        SELECT * FROM ${PasswordHistoryEntity.TABLE}
        WHERE ${PasswordHistoryEntity.Columns.USER_ID} = :userId
        ORDER BY ${PasswordHistoryEntity.Columns.CREATE_TIME} DESC
        """
    )
    abstract suspend fun getPasswordHistoryForUser(userId: String): List<PasswordHistoryEntity>

    @Query(
        """
        SELECT * FROM ${PasswordHistoryEntity.TABLE}
        WHERE ${PasswordHistoryEntity.Columns.USER_ID} = :userId
        ORDER BY ${PasswordHistoryEntity.Columns.CREATE_TIME} DESC
        """
    )
    abstract fun observePasswordHistoryForUser(userId: String): Flow<List<PasswordHistoryEntity>>

    @Query(
        """
        DELETE FROM ${PasswordHistoryEntity.TABLE}
        WHERE ${PasswordHistoryEntity.Columns.USER_ID} = :userId
        """
    )
    abstract suspend fun deletePasswordHistoryForUser(userId: String)

    @Query(
        """
        DELETE FROM ${PasswordHistoryEntity.TABLE}
        WHERE ${PasswordHistoryEntity.Columns.USER_ID} = :userId 
        AND ${PasswordHistoryEntity.Columns.ID} = :id
        """
    )
    abstract suspend fun deleteOnePasswordHistoryForUser(userId: String, id: Long)

    @Query(
        """
            DELETE FROM ${PasswordHistoryEntity.TABLE}
            WHERE ${PasswordHistoryEntity.Columns.CREATE_TIME} < :beforeTimestamp
            """
    )
    abstract suspend fun deleteHistoryOlderThan(beforeTimestamp: Long)
}
