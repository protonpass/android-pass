/*
 * Copyright (c) 2024-2026 Proton AG
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
import proton.android.pass.domain.attachments.AttachmentDownloadStatus
import proton.android.pass.domain.attachments.AttachmentDownloadStatusIndex

@Suppress("TooManyFunctions")
@Dao
abstract class AttachmentDao : BaseDao<AttachmentEntity>() {

    @Query(
        """
        DELETE FROM ${AttachmentEntity.TABLE} 
        WHERE ${AttachmentEntity.Columns.SHARE_ID} = :shareId 
          AND ${AttachmentEntity.Columns.ITEM_ID} = :itemId
        """
    )
    abstract suspend fun removeByItem(shareId: String, itemId: String)

    @Query(
        """
        SELECT ${AttachmentEntity.Columns.PERSISTENT_ID} FROM ${AttachmentEntity.TABLE}
        WHERE ${AttachmentEntity.Columns.USER_ID} = :userId
          AND ${AttachmentEntity.Columns.SHARE_ID} = :shareId
          AND ${AttachmentEntity.Columns.ITEM_ID} = :itemId
          AND ${AttachmentEntity.Columns.ID} IN (:attachmentIds)
        """
    )
    abstract fun getPersistentIdsByAttachmentIds(
        userId: String,
        shareId: String,
        itemId: String,
        attachmentIds: List<String>
    ): List<String>

    @Query(
        """
        DELETE FROM ${AttachmentEntity.TABLE}
        WHERE ${AttachmentEntity.Columns.SHARE_ID} = :shareId
          AND ${AttachmentEntity.Columns.ITEM_ID} = :itemId
      AND ${AttachmentEntity.Columns.ID} IN (:attachmentIds)
        """
    )
    abstract suspend fun removeByAttachments(
        shareId: String,
        itemId: String,
        attachmentIds: List<String>
    )

    @Query(
        """
        SELECT * FROM ${AttachmentEntity.TABLE}
        WHERE ${AttachmentEntity.Columns.SHARE_ID} = :shareId 
          AND ${AttachmentEntity.Columns.ITEM_ID} = :itemId
          AND ${AttachmentEntity.Columns.REVISION_REMOVED} IS NULL
        """
    )
    abstract fun observeActiveItemAttachments(shareId: String, itemId: String): Flow<List<AttachmentEntity>>

    @Query(
        """
        SELECT * FROM ${AttachmentEntity.TABLE}
        WHERE ${AttachmentEntity.Columns.SHARE_ID} = :shareId 
          AND ${AttachmentEntity.Columns.ITEM_ID} = :itemId
        """
    )
    abstract fun observeAllItemRevisionsAttachments(shareId: String, itemId: String): Flow<List<AttachmentEntity>>

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM ${AttachmentEntity.TABLE}
            WHERE ${AttachmentEntity.Columns.SHARE_ID} = :shareId 
              AND ${AttachmentEntity.Columns.ITEM_ID} = :itemId 
              AND ${AttachmentEntity.Columns.ID} = :persistentId
        )
        """
    )
    abstract suspend fun checkIfAttachmentExists(
        shareId: String,
        itemId: String,
        persistentId: String
    ): Boolean

    @Query(
        """
        SELECT * FROM ${AttachmentEntity.TABLE}
        WHERE ${AttachmentEntity.Columns.SHARE_ID} = :shareId
          AND ${AttachmentEntity.Columns.ITEM_ID} = :itemId
          AND ${AttachmentEntity.Columns.ID} = :attachmentId
        """
    )
    abstract suspend fun getAttachmentById(
        shareId: String,
        itemId: String,
        attachmentId: String
    ): AttachmentEntity?

    @Query(
        """
        SELECT * FROM ${AttachmentEntity.TABLE}
        WHERE ${AttachmentEntity.Columns.ID} IN (:attachmentIds)
        """
    )
    abstract suspend fun getAttachmentsByIds(attachmentIds: List<String>): List<AttachmentEntity>

    @Query(
        """
        SELECT * FROM ${AttachmentEntity.TABLE}
        WHERE ${AttachmentEntity.Columns.USER_ID} = :userId
          AND (
            ${AttachmentEntity.Columns.DOWNLOAD_STATUS} = ${AttachmentDownloadStatusIndex.PENDING}
            OR (:includeFailed AND ${AttachmentEntity.Columns.DOWNLOAD_STATUS} = ${AttachmentDownloadStatusIndex.FAILED})
          )
          AND ${AttachmentEntity.Columns.REVISION_REMOVED} IS NULL
          AND ${AttachmentEntity.Columns.SHARE_ID} IN (:shareIds)
        """
    )
    abstract fun observePendingDownloads(
        userId: String,
        shareIds: List<String>,
        includeFailed: Boolean
    ): Flow<List<AttachmentEntity>>

    @Query(
        """
        UPDATE ${AttachmentEntity.TABLE}
        SET ${AttachmentEntity.Columns.DOWNLOAD_STATUS} = :downloadStatus
        WHERE ${AttachmentEntity.Columns.USER_ID} = :userId
          AND ${AttachmentEntity.Columns.SHARE_ID} = :shareId
          AND ${AttachmentEntity.Columns.ITEM_ID} = :itemId
          AND ${AttachmentEntity.Columns.ID} = :attachmentId
        """
    )
    abstract suspend fun updateDownloadStatus(
        userId: String,
        shareId: String,
        itemId: String,
        attachmentId: String,
        downloadStatus: AttachmentDownloadStatus
    )

    @Query(
        """
        UPDATE ${AttachmentEntity.TABLE}
        SET ${AttachmentEntity.Columns.DOWNLOAD_STATUS} = :downloadStatus
        WHERE ${AttachmentEntity.Columns.USER_ID} = :userId
          AND ${AttachmentEntity.Columns.ID} IN (:attachmentIds)
        """
    )
    abstract suspend fun updateDownloadStatusForIds(
        userId: String,
        attachmentIds: List<String>,
        downloadStatus: AttachmentDownloadStatus
    )

    @Query(
        """
        UPDATE ${AttachmentEntity.TABLE}
        SET ${AttachmentEntity.Columns.DOWNLOAD_STATUS} = :newStatus
        WHERE ${AttachmentEntity.Columns.USER_ID} = :userId
          AND ${AttachmentEntity.Columns.DOWNLOAD_STATUS} = :currentStatus
        """
    )
    abstract suspend fun resetDownloadStatusForUser(
        userId: String,
        currentStatus: AttachmentDownloadStatus,
        newStatus: AttachmentDownloadStatus
    )

    @Query(
        """
        UPDATE ${AttachmentEntity.TABLE}
        SET ${AttachmentEntity.Columns.DOWNLOAD_STATUS} = :newStatus
        WHERE ${AttachmentEntity.Columns.USER_ID} = :userId
          AND ${AttachmentEntity.Columns.DOWNLOAD_STATUS} = :currentStatus
          AND ${AttachmentEntity.Columns.SHARE_ID} IN (:shareIds)
        """
    )
    abstract suspend fun resetDownloadStatusForShares(
        userId: String,
        currentStatus: AttachmentDownloadStatus,
        newStatus: AttachmentDownloadStatus,
        shareIds: List<String>
    )

    @Query(
        """
        SELECT * FROM ${AttachmentEntity.TABLE}
        WHERE ${AttachmentEntity.Columns.USER_ID} = :userId
          AND ${AttachmentEntity.Columns.REVISION_REMOVED} IS NULL
          AND ${AttachmentEntity.Columns.SHARE_ID} IN (:shareIds)
        """
    )
    abstract fun observeAllActiveAttachments(userId: String, shareIds: List<String>): Flow<List<AttachmentEntity>>

    @Query(
        """
        SELECT COUNT(*) FROM ${AttachmentEntity.TABLE}
        WHERE ${AttachmentEntity.Columns.USER_ID} = :userId
          AND ${AttachmentEntity.Columns.DOWNLOAD_STATUS} = ${AttachmentDownloadStatusIndex.DOWNLOADED}
          AND ${AttachmentEntity.Columns.REVISION_REMOVED} IS NULL
          AND ${AttachmentEntity.Columns.SHARE_ID} IN (:shareIds)
        """
    )
    abstract fun observeDownloadedCount(userId: String, shareIds: List<String>): Flow<Int>

    @Query(
        """
        SELECT COUNT(*) FROM ${AttachmentEntity.TABLE}
        WHERE ${AttachmentEntity.Columns.USER_ID} = :userId
          AND ${AttachmentEntity.Columns.REVISION_REMOVED} IS NULL
          AND ${AttachmentEntity.Columns.SHARE_ID} IN (:shareIds)
        """
    )
    abstract fun observeTotalCount(userId: String, shareIds: List<String>): Flow<Int>

    @Query(
        """
        SELECT * FROM ${AttachmentEntity.TABLE}
        WHERE ${AttachmentEntity.Columns.USER_ID} = :userId
          AND ${AttachmentEntity.Columns.SHARE_ID} = :shareId
          AND ${AttachmentEntity.Columns.ITEM_ID} = :itemId
          AND ${AttachmentEntity.Columns.ID} = :attachmentId
        """
    )
    abstract fun observeAttachmentById(
        userId: String,
        shareId: String,
        itemId: String,
        attachmentId: String
    ): Flow<AttachmentEntity?>
}
