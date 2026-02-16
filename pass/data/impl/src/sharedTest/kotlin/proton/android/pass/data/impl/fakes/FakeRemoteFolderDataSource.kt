/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.data.impl.fakes

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.fakes.mother.FolderApiModelTestFactory
import proton.android.pass.data.impl.remote.RemoteFolderDataSource
import proton.android.pass.data.impl.requests.CreateFolderRequest
import proton.android.pass.data.impl.requests.DeleteFoldersRequest
import proton.android.pass.data.impl.requests.MoveFolderRequest
import proton.android.pass.data.impl.requests.UpdateFolderRequest
import proton.android.pass.data.impl.responses.FolderApiModel
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId
import java.util.ArrayDeque

class FakeRemoteFolderDataSource : RemoteFolderDataSource {
    data class CreateFolderCall(
        val userId: UserId,
        val shareId: ShareId,
        val request: CreateFolderRequest
    )

    data class UpdateFolderCall(
        val userId: UserId,
        val shareId: ShareId,
        val folderId: FolderId,
        val request: UpdateFolderRequest
    )

    data class MoveFolderCall(
        val userId: UserId,
        val shareId: ShareId,
        val folderId: FolderId,
        val request: MoveFolderRequest
    )

    data class DeleteFoldersCall(
        val userId: UserId,
        val shareId: ShareId,
        val request: DeleteFoldersRequest
    )

    val retrieveFoldersResponses = ArrayDeque<RemoteFolderDataSource.FoldersPage>()
    val createFolderCalls = mutableListOf<CreateFolderCall>()
    val updateFolderCalls = mutableListOf<UpdateFolderCall>()
    val moveFolderCalls = mutableListOf<MoveFolderCall>()
    val deleteFolderCalls = mutableListOf<DeleteFoldersCall>()

    var createFolderResponse: FolderApiModel = FolderApiModelTestFactory.create(
        folderId = "created",
        parentFolderId = null,
        keyRotation = 1,
        folderKey = "folder-key"
    )
    var updateFolderResponse: FolderApiModel = FolderApiModelTestFactory.create(
        folderId = "updated",
        parentFolderId = null,
        keyRotation = 1,
        folderKey = "folder-key"
    )
    var moveFolderResponse: FolderApiModel = FolderApiModelTestFactory.create(
        folderId = "moved",
        parentFolderId = null,
        keyRotation = 1,
        folderKey = "folder-key"
    )

    override suspend fun retrieveFolders(
        userId: UserId,
        shareId: ShareId,
        sinceToken: String?,
        pageSize: Int?
    ): RemoteFolderDataSource.FoldersPage {
        return if (retrieveFoldersResponses.isEmpty()) {
            RemoteFolderDataSource.FoldersPage(emptyList(), null)
        } else {
            retrieveFoldersResponses.removeFirst()
        }
    }

    override suspend fun retrieveFolder(userId: UserId, shareId: ShareId, folderId: FolderId): FolderApiModel {
        error("Not needed in tests")
    }

    override suspend fun createFolder(userId: UserId, shareId: ShareId, request: CreateFolderRequest): FolderApiModel {
        createFolderCalls.add(CreateFolderCall(userId, shareId, request))
        return createFolderResponse
    }

    override suspend fun updateFolder(
        userId: UserId,
        shareId: ShareId,
        folderId: FolderId,
        request: UpdateFolderRequest
    ): FolderApiModel {
        updateFolderCalls.add(UpdateFolderCall(userId, shareId, folderId, request))
        return updateFolderResponse
    }

    override suspend fun moveFolder(
        userId: UserId,
        shareId: ShareId,
        folderId: FolderId,
        request: MoveFolderRequest
    ): FolderApiModel {
        moveFolderCalls.add(MoveFolderCall(userId, shareId, folderId, request))
        return moveFolderResponse
    }

    override suspend fun deleteFolders(userId: UserId, shareId: ShareId, request: DeleteFoldersRequest) {
        deleteFolderCalls.add(DeleteFoldersCall(userId, shareId, request))
    }
}
