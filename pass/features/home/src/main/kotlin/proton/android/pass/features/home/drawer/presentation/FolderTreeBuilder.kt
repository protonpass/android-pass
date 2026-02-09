package proton.android.pass.features.home.drawer.presentation

import proton.android.pass.commonuimodels.api.FolderUiModel
import proton.android.pass.domain.Folder

object FolderTreeBuilder {
    fun build(folders: List<Folder>): List<FolderUiModel> {
        if (folders.isEmpty()) return emptyList()

        val folderIds = folders.mapTo(mutableSetOf()) { it.folderId.id }
        val childrenMap = folders.groupBy { it.parentFolderId?.id }
        val globallyVisited = mutableSetOf<String>()

        val roots = folders.filter { folder ->
            val parentId = folder.parentFolderId?.id
            parentId == null || parentId !in folderIds
        }.ifEmpty { folders }

        return roots.mapNotNull { root ->
            buildSubtree(
                folder = root,
                childrenMap = childrenMap,
                globallyVisited = globallyVisited,
                lineage = emptySet()
            )
        }
    }

    private fun buildSubtree(
        folder: Folder,
        childrenMap: Map<String?, List<Folder>>,
        globallyVisited: MutableSet<String>,
        lineage: Set<String>
    ): FolderUiModel? {
        val folderId = folder.folderId.id
        if (folderId in lineage) return null
        if (!globallyVisited.add(folderId)) return null

        val children = childrenMap[folderId]
            .orEmpty()
            .mapNotNull { child ->
                buildSubtree(
                    folder = child,
                    childrenMap = childrenMap,
                    globallyVisited = globallyVisited,
                    lineage = lineage + folderId
                )
            }

        return FolderUiModel(
            id = folder.folderId,
            name = folder.name,
            folders = children
        )
    }
}
