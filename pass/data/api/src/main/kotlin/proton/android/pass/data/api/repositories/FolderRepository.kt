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

package proton.android.pass.data.api.repositories

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import proton.android.pass.domain.Folder
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId

interface FolderRepository {
    fun observeFolders(userId: UserId, shareId: ShareId): Flow<List<Folder>>

    suspend fun refreshFolders(userId: UserId, shareId: ShareId)

    fun observeFolder(
        userId: UserId,
        shareId: ShareId,
        folderId: FolderId
    ): Flow<Folder?>

    @Suppress("LongParameterList")
    suspend fun createFolder(
        userId: UserId,
        shareId: ShareId,
        parentFolderId: FolderId?,
        keyRotation: Long,
        contentFormatVersion: Int,
        content: String,
        folderKey: String
    ): Folder

    @Suppress("LongParameterList")
    suspend fun updateFolder(
        userId: UserId,
        shareId: ShareId,
        folderId: FolderId,
        keyRotation: Long,
        contentFormatVersion: Int,
        content: String
    ): Folder

    suspend fun moveFolder(
        userId: UserId,
        shareId: ShareId,
        folderId: FolderId,
        parentFolderId: FolderId?,
        folderKeys: List<Pair<Long, String>>
    ): Folder

    suspend fun deleteFolders(
        userId: UserId,
        shareId: ShareId,
        folderIds: List<FolderId>
    )

    suspend fun deleteFoldersLocally(
        userId: UserId,
        shareId: ShareId,
        folderIds: List<FolderId>
    )
}
