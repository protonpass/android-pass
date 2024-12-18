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

package proton.android.pass.data.impl.db.dao.attachment

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.proton.core.data.room.db.BaseDao
import proton.android.pass.data.impl.db.entities.attachments.ChunkEntity

@Dao
abstract class ChunkDao : BaseDao<ChunkEntity>() {

    @Query(
        """
        SELECT * FROM ${ChunkEntity.TABLE}
        WHERE ${ChunkEntity.Columns.SHARE_ID} = :shareId 
          AND ${ChunkEntity.Columns.ITEM_ID} = :itemId
        """
    )
    abstract fun observeItemChunks(shareId: String, itemId: String): Flow<List<ChunkEntity>>
}
