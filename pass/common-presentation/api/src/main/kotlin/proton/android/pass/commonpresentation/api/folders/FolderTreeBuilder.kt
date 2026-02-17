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

package proton.android.pass.commonpresentation.api.folders

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.commonuimodels.api.FolderUiModel
import proton.android.pass.domain.Folder

object FolderTreeBuilder {
    fun build(folders: List<Folder>): PersistentList<FolderUiModel> {
        if (folders.isEmpty()) return persistentListOf()

        val folderIds = folders.mapTo(mutableSetOf()) { it.folderId.id }
        val childrenMap = folders.groupBy { it.parentFolderId?.id }
        val globallyVisited = mutableSetOf<String>()

        val roots = folders.filter { folder ->
            val parentId = folder.parentFolderId?.id
            parentId == null || parentId !in folderIds
        }.sortedBy { it.name.lowercase() }

        val lineage = mutableSetOf<String>()

        return roots.mapNotNull { root ->
            buildSubtree(
                folder = root,
                childrenMap = childrenMap,
                globallyVisited = globallyVisited,
                lineage = lineage
            )
        }.toPersistentList()
    }

    private fun buildSubtree(
        folder: Folder,
        childrenMap: Map<String?, List<Folder>>,
        globallyVisited: MutableSet<String>,
        lineage: MutableSet<String>
    ): FolderUiModel? {
        val folderId = folder.folderId.id
        if (folderId in lineage) return null
        if (!globallyVisited.add(folderId)) return null

        lineage.add(folderId)
        val children = childrenMap[folderId]
            .orEmpty()
            .sortedBy { it.name.lowercase() }
            .mapNotNull { child ->
                buildSubtree(
                    folder = child,
                    childrenMap = childrenMap,
                    globallyVisited = globallyVisited,
                    lineage = lineage
                )
            }
            .toPersistentList()
        lineage.remove(folderId)

        return FolderUiModel(
            id = folder.folderId,
            name = folder.name,
            folders = children
        )
    }
}
