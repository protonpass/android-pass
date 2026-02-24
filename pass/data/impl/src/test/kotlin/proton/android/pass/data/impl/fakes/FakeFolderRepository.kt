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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.FolderRepository
import proton.android.pass.domain.Folder
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId

class FakeFolderRepository : FolderRepository {
    data class ObserveFolderCall(
        val userId: UserId,
        val shareId: ShareId,
        val folderId: FolderId
    )

    data class GetFolderHierarchyCall(
        val userId: UserId,
        val shareId: ShareId,
        val folderId: FolderId
    )

    var observeFolderResult: Folder? = null
    var lastObserveFolderCall: ObserveFolderCall? = null
    var getFolderHierarchyResult: List<Folder> = emptyList()
    var lastGetFolderHierarchyCall: GetFolderHierarchyCall? = null

    override fun observeFolders(userId: UserId, shareId: ShareId): Flow<List<Folder>> = emptyFlow()

    override fun observeFoldersByParentId(
        userId: UserId,
        shareId: ShareId,
        parentFolderId: FolderId?
    ): Flow<List<Folder>> = emptyFlow()

    override suspend fun refreshFolders(userId: UserId, shareId: ShareId) = Unit

    override fun observeFolder(
        userId: UserId,
        shareId: ShareId,
        folderId: FolderId
    ): Flow<Folder?> {
        lastObserveFolderCall = ObserveFolderCall(userId, shareId, folderId)
        return MutableStateFlow(observeFolderResult)
    }

    override suspend fun getFolderHierarchy(
        userId: UserId,
        shareId: ShareId,
        folderId: FolderId
    ): List<Folder> {
        lastGetFolderHierarchyCall = GetFolderHierarchyCall(userId, shareId, folderId)
        return getFolderHierarchyResult
    }

    override suspend fun createFolder(
        userId: UserId,
        shareId: ShareId,
        parentFolderId: FolderId?,
        folderName: String
    ): Folder = error("Not implemented")

    override suspend fun updateFolder(
        userId: UserId,
        shareId: ShareId,
        folderId: FolderId,
        folderName: String
    ): Folder = error("Not implemented")

    override suspend fun moveFolder(
        userId: UserId,
        shareId: ShareId,
        folderId: FolderId,
        newParentFolderId: FolderId?
    ): Folder = error("Not implemented")

    override suspend fun deleteFolders(
        userId: UserId,
        shareId: ShareId,
        folderIds: List<FolderId>
    ) = Unit

    override suspend fun deleteFoldersLocally(
        userId: UserId,
        shareId: ShareId,
        folderIds: List<FolderId>
    ) = Unit
}
