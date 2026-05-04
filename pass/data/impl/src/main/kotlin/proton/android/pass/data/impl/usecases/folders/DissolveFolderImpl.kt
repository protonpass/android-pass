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

import kotlinx.coroutines.flow.first
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveItems
import proton.android.pass.data.api.usecases.folders.DissolveFolder
import proton.android.pass.data.api.usecases.folders.MoveFolder
import proton.android.pass.data.api.usecases.folders.MoveItemsInsideShare
import proton.android.pass.data.api.usecases.folders.ObserveFoldersByParentId
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareSelection
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DissolveFolderImpl @Inject constructor(
    private val observeFoldersByParentId: ObserveFoldersByParentId,
    private val moveFolder: MoveFolder,
    private val observeItems: ObserveItems,
    private val moveItemsInsideShare: MoveItemsInsideShare
) : DissolveFolder {

    override suspend fun invoke(shareId: ShareId, folderId: FolderId) {
        val childFolders = observeFoldersByParentId(shareId, parentFolderId = folderId).first()
        childFolders.forEach { folder ->
            moveFolder(shareId = shareId, folderId = folder.folderId, newParentFolderId = null)
        }

        val directItems = observeItems(
            selection = ShareSelection.Folder(shareId, folderId),
            itemState = ItemState.Active,
            filter = ItemTypeFilter.All,
            includeHidden = true
        ).first()

        if (directItems.isNotEmpty()) {
            moveItemsInsideShare(
                shareId = shareId,
                folderId = null,
                itemIds = directItems.map { it.id }
            )
        }
    }
}
