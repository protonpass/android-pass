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
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.db.entities.FolderEntity
import proton.android.pass.data.impl.local.LocalFolderDataSource
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId

class FakeLocalFolderDataSource : LocalFolderDataSource {
    data class DeleteCall(
        val userId: UserId,
        val shareId: ShareId,
        val folderIds: List<FolderId>
    )

    private val folderByShareAndId = linkedMapOf<Pair<String, String>, FolderEntity>()
    private val state = MutableStateFlow<List<FolderEntity>>(emptyList())

    val memory: List<FolderEntity> get() = state.value
    val deleteCalls = mutableListOf<DeleteCall>()

    var deleteFoldersResult: Boolean = true

    override suspend fun upsertFolder(folder: FolderEntity) {
        folderByShareAndId[folder.shareId to folder.id] = folder
        publish()
    }

    override suspend fun upsertFolders(folders: List<FolderEntity>) {
        folders.forEach { folderByShareAndId[it.shareId to it.id] = it }
        publish()
    }

    override fun observeFolders(userId: UserId, shareId: ShareId, parentFolderId: FolderId?): Flow<List<FolderEntity>> =
        state.map { folders ->
            folders.filter {
                it.userId == userId.id &&
                    it.shareId == shareId.id &&
                    it.parentFolderId == parentFolderId?.id
            }
        }

    override fun observeFolder(userId: UserId, shareId: ShareId, folderId: FolderId): Flow<FolderEntity?> =
        state.map { folders ->
            folders.firstOrNull {
                it.userId == userId.id &&
                    it.shareId == shareId.id &&
                    it.id == folderId.id
            }
        }

    override suspend fun getById(userId: UserId, shareId: ShareId, folderId: FolderId): FolderEntity? =
        state.value.firstOrNull {
            it.userId == userId.id &&
                it.shareId == shareId.id &&
                it.id == folderId.id
        }

    override suspend fun getByIds(userId: UserId, shareId: ShareId, folderIds: List<FolderId>): List<FolderEntity> =
        state.value.filter {
            it.userId == userId.id &&
                it.shareId == shareId.id &&
                folderIds.any { id -> id.id == it.id }
        }

    override suspend fun deleteFolders(userId: UserId, shareId: ShareId, folderIds: List<FolderId>): Boolean {
        deleteCalls.add(DeleteCall(userId, shareId, folderIds))
        if (!deleteFoldersResult) return false

        folderIds.forEach { folderByShareAndId.remove(shareId.id to it.id) }
        publish()
        return true
    }

    private fun publish() {
        state.value = folderByShareAndId.values.toList()
    }
}
