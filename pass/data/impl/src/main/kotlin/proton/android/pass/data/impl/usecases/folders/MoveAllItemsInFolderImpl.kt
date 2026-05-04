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
import proton.android.pass.common.api.toOption
import proton.android.pass.data.api.repositories.MigrateItemsResult
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.MigrateItems
import proton.android.pass.data.api.usecases.ObserveItems
import proton.android.pass.data.api.usecases.folders.MoveAllItemsInFolder
import proton.android.pass.data.api.usecases.folders.MoveItemsInsideShare
import proton.android.pass.data.api.usecases.folders.ObserveFoldersByParentId
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareSelection
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MoveAllItemsInFolderImpl @Inject constructor(
    private val observeFoldersByParentId: ObserveFoldersByParentId,
    private val observeItems: ObserveItems,
    private val moveItemsInsideShare: MoveItemsInsideShare,
    private val migrateItems: MigrateItems
) : MoveAllItemsInFolder {

    override suspend fun invoke(
        shareId: ShareId,
        folderId: FolderId,
        destShareId: ShareId,
        destFolderId: FolderId?
    ) {
        val descendantFolderIds = collectDescendantFolderIds(shareId, folderId)
        val allItemIds = (listOf(folderId) + descendantFolderIds).flatMap { currentFolderId ->
            observeItems(
                selection = ShareSelection.Folder(shareId, currentFolderId),
                itemState = ItemState.Active,
                filter = ItemTypeFilter.All,
                includeHidden = true
            ).first().map { it.id }
        }

        if (allItemIds.isEmpty()) return

        if (shareId == destShareId) {
            moveItemsInsideShare(
                shareId = shareId,
                folderId = destFolderId,
                itemIds = allItemIds
            )
        } else {
            when (
                val result = migrateItems(
                    items = mapOf(shareId to allItemIds),
                    destinationShare = destShareId,
                    destinationFolderId = destFolderId.toOption()
                )
            ) {
                is MigrateItemsResult.AllMigrated -> Unit
                is MigrateItemsResult.SomeMigrated -> Unit
                is MigrateItemsResult.NoneMigrated -> throw result.exception
            }
        }
    }

    private suspend fun collectDescendantFolderIds(shareId: ShareId, parentFolderId: FolderId): List<FolderId> {
        val result = mutableListOf<FolderId>()
        val queue = ArrayDeque<FolderId>()
        val visited = mutableSetOf(parentFolderId)
        queue.add(parentFolderId)
        while (queue.isNotEmpty()) {
            val currentId = queue.removeFirst()
            val children = observeFoldersByParentId(shareId, currentId).first()
            for (child in children) {
                if (visited.add(child.folderId)) {
                    result.add(child.folderId)
                    queue.add(child.folderId)
                }
            }
        }
        return result
    }
}
