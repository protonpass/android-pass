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

package proton.android.pass.composecomponents.impl.folders

import com.google.common.truth.Truth.assertThat
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test
import proton.android.pass.commonuimodels.api.FolderUiModel
import proton.android.pass.domain.FolderId

class FolderUtilsTest {

    // region allFolderIds

    @Test
    fun `allFolderIds returns empty set for empty list`() {
        assertThat(allFolderIds(emptyList())).isEmpty()
    }

    @Test
    fun `allFolderIds returns root-level folder ids`() {
        val folders = listOf(folder("a"), folder("b"))
        assertThat(allFolderIds(folders)).containsExactly("a", "b")
    }

    @Test
    fun `allFolderIds returns nested folder ids`() {
        val folders = listOf(
            folder("a", folder("a1"), folder("a2")),
            folder("b", folder("b1", folder("b1a")))
        )
        assertThat(allFolderIds(folders)).containsExactly("a", "a1", "a2", "b", "b1", "b1a")
    }

    // region foldersToExpand

    @Test
    fun `foldersToExpand returns empty when previousIds is null (initial load)`() {
        val folders = listOf(folder("a"), folder("b"))
        val result = foldersToExpand(null, allFolderIds(folders), folders)
        assertThat(result).isEmpty()
    }

    @Test
    fun `foldersToExpand returns empty when previousIds is empty (onStart sentinel emission)`() {
        val folders = listOf(folder("a", folder("a1")), folder("b"))
        val result = foldersToExpand(emptySet(), allFolderIds(folders), folders)
        assertThat(result).isEmpty()
    }

    @Test
    fun `foldersToExpand returns empty when no new folders`() {
        val folders = listOf(folder("a"), folder("b"))
        val ids = allFolderIds(folders)
        val result = foldersToExpand(ids, ids, folders)
        assertThat(result).isEmpty()
    }

    @Test
    fun `foldersToExpand returns empty when new root-level folder has no ancestor`() {
        val before = listOf(folder("a"))
        val after = listOf(folder("a"), folder("b"))
        val result = foldersToExpand(allFolderIds(before), allFolderIds(after), after)
        assertThat(result).isEmpty()
    }

    @Test
    fun `foldersToExpand returns parent id when subfolder is added`() {
        val before = listOf(folder("a"))
        val after = listOf(folder("a", folder("a1")))
        val result = foldersToExpand(allFolderIds(before), allFolderIds(after), after)
        assertThat(result).containsExactly("a")
    }

    @Test
    fun `foldersToExpand returns full ancestor chain when deeply nested folder is added`() {
        val before = listOf(folder("a", folder("a1")))
        val after = listOf(folder("a", folder("a1", folder("a1a"))))
        val result = foldersToExpand(allFolderIds(before), allFolderIds(after), after)
        assertThat(result).containsExactly("a", "a1")
    }

    @Test
    fun `foldersToExpand handles multiple new folders added at once`() {
        val before = listOf(folder("a"), folder("b"))
        val after = listOf(
            folder("a", folder("a1")),
            folder("b", folder("b1"))
        )
        val result = foldersToExpand(allFolderIds(before), allFolderIds(after), after)
        assertThat(result).containsExactly("a", "b")
    }

    // region helpers

    private fun folder(id: String, vararg children: FolderUiModel) = FolderUiModel(
        id = FolderId(id),
        name = id,
        folders = persistentListOf(*children)
    )
}
