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
import kotlinx.coroutines.flow.flowOf
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.db.entities.FolderKeyEntity
import proton.android.pass.data.impl.local.LocalFolderKeyDataSource
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId

class FakeLocalFolderKeyDataSource : LocalFolderKeyDataSource {
    private val keyByShareAndFolderId = mutableMapOf<Pair<String, String>, FolderKeyEntity>()

    val memory: List<FolderKeyEntity>
        get() = keyByShareAndFolderId.values.toList()

    override suspend fun upsertKey(entity: FolderKeyEntity) {
        keyByShareAndFolderId[entity.shareId to entity.folderId] = entity
    }

    override suspend fun upsertKeys(entities: List<FolderKeyEntity>) {
        entities.forEach { upsertKey(it) }
    }

    override suspend fun getByFolderId(shareId: ShareId, folderId: FolderId): FolderKeyEntity? =
        keyByShareAndFolderId[shareId.id to folderId.id]

    override suspend fun getByFolderIds(shareId: ShareId, folderIds: List<FolderId>): List<FolderKeyEntity> =
        folderIds.mapNotNull { keyByShareAndFolderId[shareId.id to it.id] }

    override suspend fun getAllByShareId(userId: UserId, shareId: ShareId): List<FolderKeyEntity> =
        keyByShareAndFolderId.values.filter { it.userId == userId.id && it.shareId == shareId.id }

    override fun observeByShareId(userId: UserId, shareId: ShareId): Flow<List<FolderKeyEntity>> =
        flowOf(
            keyByShareAndFolderId.values
                .filter { it.userId == userId.id && it.shareId == shareId.id }
        )

    override suspend fun deleteByFolderId(shareId: ShareId, folderId: FolderId) {
        keyByShareAndFolderId.remove(shareId.id to folderId.id)
    }

    override suspend fun deleteByFolderIds(shareId: ShareId, folderIds: List<FolderId>) {
        folderIds.forEach { deleteByFolderId(shareId, it) }
    }
}
