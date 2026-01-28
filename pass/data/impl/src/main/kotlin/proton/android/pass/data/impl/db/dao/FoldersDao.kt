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
import proton.android.pass.data.impl.db.entities.FolderEntity

@Dao
abstract class FoldersDao : BaseDao<FolderEntity>() {

    /**
     * Observes folders for a user and share.
     *
     * @param parentFolderId When null, returns ALL folders for the share (flat list).
     *                       When non-null, returns only folders with matching parent.
     */
    @Query(
        """
        SELECT * FROM ${FolderEntity.TABLE}
        WHERE ${FolderEntity.Columns.USER_ID} = :userId
          AND ${FolderEntity.Columns.SHARE_ID} = :shareId
          AND (:parentFolderId IS NULL OR ${FolderEntity.Columns.PARENT_FOLDER_ID} = :parentFolderId)
        ORDER BY ${FolderEntity.Columns.ID}
        """
    )
    abstract fun observeFolders(
        userId: String,
        shareId: String,
        parentFolderId: String?
    ): Flow<List<FolderEntity>>

    @Query(
        """
        SELECT * FROM ${FolderEntity.TABLE}
        WHERE ${FolderEntity.Columns.USER_ID} = :userId
          AND ${FolderEntity.Columns.SHARE_ID} = :shareId
          AND ${FolderEntity.Columns.ID} = :folderId
        LIMIT 1
        """
    )
    abstract fun observeById(
        userId: String,
        shareId: String,
        folderId: String
    ): Flow<FolderEntity?>

    @Query(
        """
        SELECT * FROM ${FolderEntity.TABLE}
        WHERE ${FolderEntity.Columns.USER_ID} = :userId
          AND ${FolderEntity.Columns.SHARE_ID} = :shareId
          AND ${FolderEntity.Columns.ID} = :folderId
        LIMIT 1
        """
    )
    abstract suspend fun getById(
        userId: String,
        shareId: String,
        folderId: String
    ): FolderEntity?

    @Query(
        """
        DELETE FROM ${FolderEntity.TABLE}
        WHERE ${FolderEntity.Columns.USER_ID} = :userId
          AND ${FolderEntity.Columns.SHARE_ID} = :shareId
          AND ${FolderEntity.Columns.ID} IN (:folderIds)
        """
    )
    abstract suspend fun deleteFolders(
        userId: String,
        shareId: String,
        folderIds: List<String>
    ): Int
}
