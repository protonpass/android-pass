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

package proton.android.pass.data.impl.repositories

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.local.LocalFolderDataSource
import proton.android.pass.data.impl.local.LocalFolderKeyDataSource
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.key.FolderKey
import javax.inject.Inject

class FolderKeyRepositoryImpl @Inject constructor(
    private val localFolderKeyDataSource: LocalFolderKeyDataSource,
    private val localFolderDataSource: LocalFolderDataSource
) : FolderKeyRepository {

    override suspend fun getFolderKey(
        userId: UserId,
        shareId: ShareId,
        folderId: FolderId
    ): FolderKey? {
        val entity = localFolderKeyDataSource.getByFolderId(userId, shareId, folderId) ?: return null
        val responseKey = localFolderDataSource.getById(userId, shareId, folderId)
            ?.folderKey
            ?: return null
        return FolderKey(
            rotation = entity.keyRotation,
            key = entity.encryptedKey,
            responseKey = responseKey
        )
    }

    override suspend fun getFolderKeys(
        userId: UserId,
        shareId: ShareId,
        folderIds: List<FolderId>
    ): Map<FolderId, FolderKey> {
        if (folderIds.isEmpty()) return emptyMap()

        val keyEntities = localFolderKeyDataSource.getByFolderIds(userId, shareId, folderIds)
            .associateBy { it.folderId }
        val folderEntities = localFolderDataSource.getByIds(userId, shareId, folderIds)
            .associateBy { it.id }

        return folderIds.mapNotNull { folderId ->
            val keyEntity = keyEntities[folderId.id] ?: return@mapNotNull null
            val responseKey = folderEntities[folderId.id]?.folderKey ?: return@mapNotNull null
            folderId to FolderKey(
                rotation = keyEntity.keyRotation,
                key = keyEntity.encryptedKey,
                responseKey = responseKey
            )
        }.toMap()
    }
}
