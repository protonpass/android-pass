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

package proton.android.pass.data.impl.db.dao.inappmessages

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.proton.core.data.room.db.BaseDao
import proton.android.pass.data.impl.db.entities.InAppMessageEntity

@Dao
abstract class InAppMessagesDao : BaseDao<InAppMessageEntity>() {

    @Query(
        """
        SELECT * 
        FROM ${InAppMessageEntity.TABLE} 
        WHERE ${InAppMessageEntity.Columns.USER_ID} = :userId
        AND (:mode IS NULL OR ${InAppMessageEntity.Columns.MODE} = :mode)
        AND ${InAppMessageEntity.Columns.STATE} != :status
        AND ${InAppMessageEntity.Columns.RANGE_START} <= :currentTimestamp
        AND (${InAppMessageEntity.Columns.RANGE_END} IS NULL OR ${InAppMessageEntity.Columns.RANGE_END} >= :currentTimestamp)
        ORDER BY ${InAppMessageEntity.Columns.PRIORITY} DESC, ${InAppMessageEntity.Columns.RANGE_START} ASC
        LIMIT 1
        """
    )
    abstract fun observeDeliverableMessagesWithNotStatus(
        userId: String,
        mode: Int?,
        status: Int,
        currentTimestamp: Long
    ): Flow<InAppMessageEntity?>

    @Query(
        """
        DELETE 
        FROM ${InAppMessageEntity.TABLE} 
        WHERE ${InAppMessageEntity.Columns.USER_ID} = :userId
        """
    )
    abstract suspend fun deleteAll(userId: String)

    @Query(
        """
        SELECT * 
        FROM ${InAppMessageEntity.TABLE} 
        WHERE ${InAppMessageEntity.Columns.USER_ID} = :userId 
        AND ${InAppMessageEntity.Columns.ID} = :messageId
        """
    )
    abstract fun observeUserMessage(userId: String, messageId: String): Flow<InAppMessageEntity>
}
