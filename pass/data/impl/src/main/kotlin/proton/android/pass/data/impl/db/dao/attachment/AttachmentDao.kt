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
import proton.android.pass.data.impl.db.entities.attachments.AttachmentEntity

@Dao
abstract class AttachmentDao : BaseDao<AttachmentEntity>() {

    @Query(
        """
        DELETE FROM ${AttachmentEntity.TABLE} 
        WHERE ${AttachmentEntity.Columns.SHARE_ID} = :shareId 
          AND ${AttachmentEntity.Columns.ITEM_ID} = :itemId
        """
    )
    abstract fun removeByItem(shareId: String, itemId: String)

    @Query(
        """
        DELETE FROM ${AttachmentEntity.TABLE} 
        WHERE ${AttachmentEntity.Columns.SHARE_ID} = :shareId 
          AND ${AttachmentEntity.Columns.ITEM_ID} = :itemId 
      AND ${AttachmentEntity.Columns.ID} IN (:attachmentIds)
        """
    )
    abstract fun removeByAttachments(
        shareId: String,
        itemId: String,
        attachmentIds: List<String>
    )

    @Query(
        """
        SELECT * FROM ${AttachmentEntity.TABLE}
        WHERE ${AttachmentEntity.Columns.SHARE_ID} = :shareId 
          AND ${AttachmentEntity.Columns.ITEM_ID} = :itemId
        """
    )
    abstract fun observeItemAttachments(shareId: String, itemId: String): Flow<List<AttachmentEntity>>

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM ${AttachmentEntity.TABLE}
            WHERE ${AttachmentEntity.Columns.SHARE_ID} = :shareId 
              AND ${AttachmentEntity.Columns.ITEM_ID} = :itemId 
              AND ${AttachmentEntity.Columns.ID} = :attachmentId
        )
        """
    )
    abstract fun checkIfAttachmentExists(
        shareId: String,
        itemId: String,
        attachmentId: String
    ): Boolean
}
