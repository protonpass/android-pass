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

import proton.android.pass.domain.FolderId
import proton.android.pass.domain.FolderWithItemCount

val mockFolders = buildList {
    add(
        FolderWithItemCount(
            id = FolderId("level0-0"),
            name = "My main 1",
            activeItemCount = 1,
            folders = buildList {
                add(
                    FolderWithItemCount(
                        id = FolderId("level1-0"),
                        name = "My sub folder 1",
                        activeItemCount = 2,
                        folders = emptyList()
                    )
                )
                add(
                    FolderWithItemCount(
                        id = FolderId("level1-1"),
                        name = "My sub folder 2",
                        folders = buildList {
                            add(
                                FolderWithItemCount(
                                    id = FolderId("level2-0"),
                                    name = "level2-0",
                                    activeItemCount = 20,
                                    folders = buildList {
                                        add(
                                            FolderWithItemCount(
                                                id = FolderId("level3-0"),
                                                name = "level3-0 supppperrrrr looooonnnnnng",
                                                activeItemCount = 2,
                                                folders = emptyList()
                                            )
                                        )
                                        add(
                                            FolderWithItemCount(
                                                id = FolderId("level3-1"),
                                                name = "level3-1 supppperrrrr looooonnnnnng",
                                                activeItemCount = 2,
                                                folders = emptyList()
                                            )
                                        )
                                    }
                                )
                            )
                            add(
                                FolderWithItemCount(
                                    id = FolderId("level2-1"),
                                    name = "level2-1",
                                    activeItemCount = 2
                                )
                            )
                        }
                    )
                )
                add(
                    FolderWithItemCount(
                        id = FolderId("level1-2"),
                        activeItemCount = 3,
                        name = "My sub folder 3",
                        folders = emptyList()
                    )
                )
            }
        )
    )
    add(
        FolderWithItemCount(
            id = FolderId("level0-1"),
            name = "My main 2",
            folders = emptyList()
        )
    )
    add(
        FolderWithItemCount(
            id = FolderId("level0-2"),
            name = "My main 3",
            folders = emptyList()
        )
    )
}
