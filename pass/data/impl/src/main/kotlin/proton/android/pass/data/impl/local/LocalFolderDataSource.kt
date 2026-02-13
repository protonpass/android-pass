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

package proton.android.pass.data.impl.local

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.db.entities.FolderEntity
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId

interface LocalFolderDataSource {
    suspend fun upsertFolder(folder: FolderEntity)

    suspend fun upsertFolders(folders: List<FolderEntity>)

    fun observeFolders(
        userId: UserId,
        shareId: ShareId,
        parentFolderId: FolderId?
    ): Flow<List<FolderEntity>>

    fun observeFolder(
        userId: UserId,
        shareId: ShareId,
        folderId: FolderId
    ): Flow<FolderEntity?>

    suspend fun getById(
        userId: UserId,
        shareId: ShareId,
        folderId: FolderId
    ): FolderEntity?

    suspend fun getByIds(
        userId: UserId,
        shareId: ShareId,
        folderIds: List<FolderId>
    ): List<FolderEntity>

    suspend fun deleteFolders(
        userId: UserId,
        shareId: ShareId,
        folderIds: List<FolderId>
    ): Boolean
}
