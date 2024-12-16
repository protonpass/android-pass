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
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import me.proton.core.data.room.db.BaseDao
import proton.android.pass.data.impl.db.entities.attachments.AttachmentEntity
import proton.android.pass.data.impl.db.entities.attachments.AttachmentWithChunks

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
          AND ${AttachmentEntity.Columns.ID} = :attachmentId
        """
    )
    abstract fun removeByAttachment(
        shareId: String,
        itemId: String,
        attachmentId: String
    )

    @Query(
        """
        SELECT * FROM ${AttachmentEntity.TABLE}
        WHERE ${AttachmentEntity.Columns.SHARE_ID} = :shareId 
          AND ${AttachmentEntity.Columns.ITEM_ID} = :itemId
        """
    )
    @Transaction
    abstract fun observeAttachmentsWithChunks(shareId: String, itemId: String): Flow<List<AttachmentWithChunks>>
}
