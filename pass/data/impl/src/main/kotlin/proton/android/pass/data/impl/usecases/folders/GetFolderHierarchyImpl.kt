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

package proton.android.pass.data.impl.usecases.folders

import proton.android.pass.data.api.repositories.FolderRepository
import proton.android.pass.data.api.usecases.GetItemById
import proton.android.pass.data.api.usecases.folders.GetFolderHierarchy
import proton.android.pass.domain.Folder
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import javax.inject.Inject

class GetFolderHierarchyImpl @Inject constructor(
    private val getItemById: GetItemById,
    private val folderRepository: FolderRepository
) : GetFolderHierarchy {

    override suspend fun invoke(shareId: ShareId, itemId: ItemId): List<Folder> {
        val item = getItemById(
            shareId = shareId,
            itemId = itemId
        )
        val folderId = item.folderId ?: return emptyList()

        return folderRepository.getFolderHierarchy(
            userId = item.userId,
            shareId = shareId,
            folderId = folderId
        )
    }
}
