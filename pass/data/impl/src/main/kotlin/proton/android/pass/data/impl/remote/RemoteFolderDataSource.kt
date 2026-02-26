/*
 * Copyright (c) 2025-2026 Proton AG
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

package proton.android.pass.data.impl.remote

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.requests.CreateFolderRequest
import proton.android.pass.data.impl.requests.DeleteFoldersRequest
import proton.android.pass.data.impl.requests.MoveFolderRequest
import proton.android.pass.data.impl.requests.UpdateFolderRequest
import proton.android.pass.data.impl.responses.FolderApiModel
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId

interface RemoteFolderDataSource {
    suspend fun retrieveFolders(
        userId: UserId,
        shareId: ShareId,
        sinceToken: String?,
        pageSize: Int?
    ): FoldersPage

    data class FoldersPage(
        val folders: List<FolderApiModel>,
        val lastToken: String?
    )

    suspend fun retrieveFolder(
        userId: UserId,
        shareId: ShareId,
        folderId: FolderId
    ): FolderApiModel

    suspend fun createFolder(
        userId: UserId,
        shareId: ShareId,
        request: CreateFolderRequest
    ): FolderApiModel

    suspend fun updateFolder(
        userId: UserId,
        shareId: ShareId,
        folderId: FolderId,
        request: UpdateFolderRequest
    ): FolderApiModel

    suspend fun moveFolder(
        userId: UserId,
        shareId: ShareId,
        folderId: FolderId,
        request: MoveFolderRequest
    ): FolderApiModel

    suspend fun deleteFolders(
        userId: UserId,
        shareId: ShareId,
        request: DeleteFoldersRequest
    )
}
