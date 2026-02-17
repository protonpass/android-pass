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
import proton.android.pass.data.impl.db.entities.FolderKeyEntity

@Dao
abstract class FolderKeysDao : BaseDao<FolderKeyEntity>() {

    @Query(
        """
        SELECT * FROM ${FolderKeyEntity.TABLE}
        WHERE ${FolderKeyEntity.Columns.USER_ID} = :userId
          AND ${FolderKeyEntity.Columns.SHARE_ID} = :shareId
          AND ${FolderKeyEntity.Columns.FOLDER_ID} = :folderId
        LIMIT 1
        """
    )
    abstract suspend fun getByFolderId(userId: String, shareId: String, folderId: String): FolderKeyEntity?

    @Query(
        """
        SELECT * FROM ${FolderKeyEntity.TABLE}
        WHERE ${FolderKeyEntity.Columns.USER_ID} = :userId
          AND ${FolderKeyEntity.Columns.SHARE_ID} = :shareId
        """
    )
    abstract fun observeByShareId(userId: String, shareId: String): Flow<List<FolderKeyEntity>>

    @Query(
        """
        SELECT * FROM ${FolderKeyEntity.TABLE}
        WHERE ${FolderKeyEntity.Columns.USER_ID} = :userId
          AND ${FolderKeyEntity.Columns.SHARE_ID} = :shareId
        """
    )
    abstract suspend fun getAllByShareId(userId: String, shareId: String): List<FolderKeyEntity>

    @Query(
        """
        SELECT * FROM ${FolderKeyEntity.TABLE}
        WHERE ${FolderKeyEntity.Columns.USER_ID} = :userId
          AND ${FolderKeyEntity.Columns.SHARE_ID} = :shareId
          AND ${FolderKeyEntity.Columns.FOLDER_ID} IN (:folderIds)
        """
    )
    abstract suspend fun getByFolderIds(userId: String, shareId: String, folderIds: List<String>): List<FolderKeyEntity>

    @Query(
        """
        DELETE FROM ${FolderKeyEntity.TABLE}
        WHERE ${FolderKeyEntity.Columns.USER_ID} = :userId
          AND ${FolderKeyEntity.Columns.SHARE_ID} = :shareId
          AND ${FolderKeyEntity.Columns.FOLDER_ID} = :folderId
        """
    )
    abstract suspend fun deleteByFolderId(userId: String, shareId: String, folderId: String): Int

    @Query(
        """
        DELETE FROM ${FolderKeyEntity.TABLE}
        WHERE ${FolderKeyEntity.Columns.USER_ID} = :userId
          AND ${FolderKeyEntity.Columns.SHARE_ID} = :shareId
          AND ${FolderKeyEntity.Columns.FOLDER_ID} IN (:folderIds)
        """
    )
    abstract suspend fun deleteByFolderIds(userId: String, shareId: String, folderIds: List<String>): Int

    @Query("DELETE FROM ${FolderKeyEntity.TABLE}")
    abstract suspend fun deleteAll(): Int
}
