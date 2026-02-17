/*
 * Copyright (c) 2025-2026 Proton AG
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

package proton.android.pass.composecomponents.impl.folders.mock

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.commonuimodels.api.FolderUiModel
import proton.android.pass.domain.FolderId

class ThemedFoldersPreviewProvider : ThemePairPreviewProvider<FoldersParameter>(
    provider = FoldersPreviewProvider()
)

class FoldersPreviewProvider : PreviewParameterProvider<FoldersParameter> {
    override val values: Sequence<FoldersParameter>
        get() = sequenceOf(
            withEmptyFolders(),
            withSingleFolder(),
            withNestedFolders(),
            withNestedFoldersExpanded()
        )

    companion object {
        private fun withEmptyFolders() = FoldersParameter(
            folders = emptyList()
        )

        private fun withSingleFolder() = FoldersParameter(
            folders = listOf(
                FolderUiModel(
                    id = FolderId("level0-0"),
                    name = "My main folder",
                    folders = emptyList()
                )
            )
        )

        private fun withNestedFolders(): FoldersParameter {
            val level3Folders = listOf(
                createFolder("level3-0", "level3-0 supppperrrrr looooonnnnnng"),
                createFolder("level3-1", "level3-1 supppperrrrr looooonnnnnng")
            )
            val level2Folders = listOf(
                createFolder("level2-0", "level2-0", level3Folders),
                createFolder("level2-1", "level2-1")
            )
            val level1Folders = listOf(
                createFolder("level1-0", "My sub folder 1"),
                createFolder("level1-1", "My sub folder 2", level2Folders),
                createFolder("level1-2", "My sub folder 3")
            )
            val level0Folders = listOf(
                createFolder("level0-0", "My main 1", level1Folders),
                createFolder("level0-1", "My main 2"),
                createFolder("level0-2", "My main 3")
            )
            return FoldersParameter(folders = level0Folders)
        }

        private fun withNestedFoldersExpanded(): FoldersParameter {
            val level3Folders = listOf(
                createFolder("level3-0", "level3-0 supppperrrrr looooonnnnnng"),
                createFolder("level3-1", "level3-1 supppperrrrr looooonnnnnng")
            )
            val level2Folders = listOf(
                createFolder("level2-0", "level2-0", level3Folders),
                createFolder("level2-1", "level2-1")
            )
            val level1Folders = listOf(
                createFolder("level1-0", "My sub folder 1"),
                createFolder("level1-1", "My sub folder 2", level2Folders),
                createFolder("level1-2", "My sub folder 3")
            )
            val level0Folders = listOf(
                createFolder("level0-0", "My main 1", level1Folders),
                createFolder("level0-1", "My main 2"),
                createFolder("level0-2", "My main 3")
            )
            val expandedState = mapOf(
                "level0-0" to true,
                "level1-1" to true,
                "level2-0" to true
            )
            return FoldersParameter(
                folders = level0Folders,
                expandedState = expandedState
            )
        }

        private fun createFolder(
            id: String,
            name: String,
            folders: List<FolderUiModel> = emptyList()
        ) = FolderUiModel(
            id = FolderId(id),
            name = name,
            folders = folders
        )
    }
}

data class FoldersParameter(
    val folders: List<FolderUiModel>,
    val expandedState: Map<String, Boolean> = emptyMap()
)
