/*
 * Copyright (c) 2024-2026 Proton AG
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

package proton.android.pass.composecomponents.impl.folders

import proton.android.pass.commonuimodels.api.FolderUiModel
import proton.android.pass.domain.FolderId

data class FlatFolderItem(val folder: FolderUiModel, val depth: Int)

fun flattenVisibleFolders(
    folders: List<FolderUiModel>,
    expandedState: Map<String, Boolean>,
    depth: Int = 0
): List<FlatFolderItem> {
    val result = mutableListOf<FlatFolderItem>()
    for (folder in folders) {
        result.add(FlatFolderItem(folder, depth))
        if (expandedState[folder.id.id] == true && folder.folders.isNotEmpty()) {
            result.addAll(flattenVisibleFolders(folder.folders, expandedState, depth + 1))
        }
    }
    return result
}

/**
 * Wraps a flat map so that all key operations are transparently namespaced.
 * Used to share a single SnapshotStateMap across multiple vaults while keeping
 * each vault's folder expand state isolated (folder IDs are only unique per vault).
 */
class NamespacedExpandedState(
    private val delegate: MutableMap<String, Boolean>,
    private val namespace: String
) : AbstractMutableMap<String, Boolean>() {

    override val entries: MutableSet<MutableMap.MutableEntry<String, Boolean>>
        get() = delegate.entries
            .filter { it.key.startsWith("$namespace::") }
            .mapTo(mutableSetOf()) { delegateEntry ->
                object : MutableMap.MutableEntry<String, Boolean> {
                    override val key = delegateEntry.key.removePrefix("$namespace::")
                    override val value get() = delegateEntry.value
                    override fun setValue(newValue: Boolean) = delegateEntry.setValue(newValue)
                }
            }

    private fun key(k: String) = "$namespace::$k"

    override fun put(key: String, value: Boolean) = delegate.put(key(key), value)
    override fun get(key: String) = delegate[key(key)]
    override fun containsKey(key: String) = delegate.containsKey(key(key))
    override fun remove(key: String) = delegate.remove(key(key))
}

fun allFolderIds(folders: List<FolderUiModel>): Set<String> {
    val ids = mutableSetOf<String>()
    fun traverse(list: List<FolderUiModel>) {
        for (folder in list) {
            ids.add(folder.id.id)
            traverse(folder.folders)
        }
    }
    traverse(folders)
    return ids
}

fun foldersToExpand(
    previousIds: Set<String>?,
    currentIds: Set<String>,
    folders: List<FolderUiModel>
): Set<String> {
    if (previousIds.isNullOrEmpty()) return emptySet()
    val newIds = currentIds - previousIds
    if (newIds.isEmpty()) return emptySet()
    val toExpand = mutableSetOf<String>()
    newIds.forEach { newId ->
        val expandedState = mutableMapOf<String, Boolean>()
        expandAncestors(folders, FolderId(newId), expandedState)
        toExpand += expandedState.keys
    }
    return toExpand
}

fun containsFolderId(folders: List<FolderUiModel>, folderId: FolderId): Boolean {
    for (folder in folders) {
        if (folder.id == folderId) return true
        if (containsFolderId(folder.folders, folderId)) return true
    }
    return false
}

fun expandAncestors(
    folders: List<FolderUiModel>,
    targetId: FolderId,
    expandedState: MutableMap<String, Boolean>
): Boolean {
    for (folder in folders) {
        if (folder.id == targetId) return true
        if (expandAncestors(folder.folders, targetId, expandedState)) {
            expandedState[folder.id.id] = true
            return true
        }
    }
    return false
}
