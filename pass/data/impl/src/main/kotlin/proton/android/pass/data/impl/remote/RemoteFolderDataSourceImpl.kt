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

package proton.android.pass.data.impl.remote

import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import proton.android.pass.data.impl.api.PasswordManagerApi
import proton.android.pass.data.impl.requests.CreateFolderRequest
import proton.android.pass.data.impl.requests.DeleteFoldersRequest
import proton.android.pass.data.impl.requests.MoveFolderRequest
import proton.android.pass.data.impl.requests.UpdateFolderRequest
import proton.android.pass.data.impl.responses.FolderApiModel
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId
import javax.inject.Inject

class RemoteFolderDataSourceImpl @Inject constructor(
    private val api: ApiProvider
) : RemoteFolderDataSource {

    override suspend fun retrieveFolders(
        userId: UserId,
        shareId: ShareId,
        sinceToken: String?,
        pageSize: Int?
    ): RemoteFolderDataSource.FoldersPage {
        val response = api.get<PasswordManagerApi>(userId)
            .invoke { getFolders(shareId.id, sinceToken, pageSize) }
            .valueOrThrow
        return RemoteFolderDataSource.FoldersPage(
            folders = response.folders.folders,
            lastToken = response.folders.lastToken
        )
    }

    override suspend fun retrieveFolder(
        userId: UserId,
        shareId: ShareId,
        folderId: FolderId
    ): FolderApiModel = api.get<PasswordManagerApi>(userId)
        .invoke { getFolder(shareId.id, folderId.id) }
        .valueOrThrow
        .folder

    override suspend fun createFolder(
        userId: UserId,
        shareId: ShareId,
        request: CreateFolderRequest
    ): FolderApiModel = api.get<PasswordManagerApi>(userId)
        .invoke { createFolder(shareId.id, request) }
        .valueOrThrow
        .folder

    override suspend fun updateFolder(
        userId: UserId,
        shareId: ShareId,
        folderId: FolderId,
        request: UpdateFolderRequest
    ): FolderApiModel = api.get<PasswordManagerApi>(userId)
        .invoke { updateFolder(shareId.id, folderId.id, request) }
        .valueOrThrow
        .folder

    override suspend fun moveFolder(
        userId: UserId,
        shareId: ShareId,
        folderId: FolderId,
        request: MoveFolderRequest
    ): FolderApiModel = api.get<PasswordManagerApi>(userId)
        .invoke { moveFolder(shareId.id, folderId.id, request) }
        .valueOrThrow
        .folder

    override suspend fun deleteFolders(
        userId: UserId,
        shareId: ShareId,
        request: DeleteFoldersRequest
    ) {
        api.get<PasswordManagerApi>(userId)
            .invoke { deleteFolders(shareId.id, request) }
            .valueOrThrow
    }
}
