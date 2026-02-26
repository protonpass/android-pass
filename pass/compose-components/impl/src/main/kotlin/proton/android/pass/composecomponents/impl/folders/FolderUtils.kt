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
