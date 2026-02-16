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

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import proton.android.pass.domain.FolderId
import proton.android.pass.commonuimodels.api.FolderUiModel
import proton.android.pass.test.domain.FolderTestFactory

class FolderTreeBuilderTest {

    @Test
    fun `build returns empty list when input is empty`() {
        val result = FolderTreeBuilder.build(emptyList())

        assertThat(result).isEmpty()
    }

    @Test
    fun `build creates simple tree structure`() {
        val root = FolderTestFactory.create(
            folderId = FolderId("root"),
            parentFolderId = null,
            name = "Root"
        )
        val child = FolderTestFactory.create(
            folderId = FolderId("child"),
            parentFolderId = FolderId("root"),
            name = "Child"
        )

        val result = FolderTreeBuilder.build(listOf(root, child))

        assertThat(result).hasSize(1)
        assertThat(result[0].id).isEqualTo(FolderId("root"))
        assertThat(result[0].name).isEqualTo("Root")
        assertThat(result[0].folders).hasSize(1)
        assertThat(result[0].folders[0].id).isEqualTo(FolderId("child"))
        assertThat(result[0].folders[0].name).isEqualTo("Child")
    }

    @Test
    fun `build handles circular references`() {
        // Create a circular reference: folder1 -> folder2 -> folder1
        val folder1 = FolderTestFactory.create(
            folderId = FolderId("folder1"),
            parentFolderId = FolderId("folder2"),
            name = "Folder 1"
        )
        val folder2 = FolderTestFactory.create(
            folderId = FolderId("folder2"),
            parentFolderId = FolderId("folder1"),
            name = "Folder 2"
        )

        val result = FolderTreeBuilder.build(listOf(folder1, folder2))

        // Circular folders are excluded from the tree entirely
        // The cycle detection returns null for cyclic nodes, preventing infinite recursion
        assertThat(result).isEmpty()
    }

    @Test
    fun `build handles self-referencing folder`() {
        val selfRef = FolderTestFactory.create(
            folderId = FolderId("self"),
            parentFolderId = FolderId("self"),
            name = "Self Reference"
        )

        val result = FolderTreeBuilder.build(listOf(selfRef))

        // Self-referencing folder is excluded from the tree entirely
        // The cycle detection (lineage check) prevents it from being added
        assertThat(result).isEmpty()
    }

    @Test
    fun `build handles orphaned folders`() {
        // Create folders where parent doesn't exist
        val orphan1 = FolderTestFactory.create(
            folderId = FolderId("orphan1"),
            parentFolderId = FolderId("missing-parent-1"),
            name = "Orphan 1"
        )
        val orphan2 = FolderTestFactory.create(
            folderId = FolderId("orphan2"),
            parentFolderId = FolderId("missing-parent-2"),
            name = "Orphan 2"
        )
        val normalRoot = FolderTestFactory.create(
            folderId = FolderId("root"),
            parentFolderId = null,
            name = "Root"
        )

        val result = FolderTreeBuilder.build(listOf(orphan1, orphan2, normalRoot))

        // All orphaned folders should be treated as roots
        assertThat(result).hasSize(3)
        val ids = result.map { it.id.id }
        assertThat(ids).containsExactly("orphan1", "orphan2", "root")
    }

    @Test
    fun `build handles partially orphaned chain`() {
        // Create a chain where middle parent is missing: root -> missing -> child
        val root = FolderTestFactory.create(
            folderId = FolderId("root"),
            parentFolderId = null,
            name = "Root"
        )
        val orphanedChild = FolderTestFactory.create(
            folderId = FolderId("child"),
            parentFolderId = FolderId("missing-middle"),
            name = "Orphaned Child"
        )

        val result = FolderTreeBuilder.build(listOf(root, orphanedChild))

        // Both should be roots since the child's parent doesn't exist
        assertThat(result).hasSize(2)
        assertThat(result[0].id).isEqualTo(FolderId("root"))
        assertThat(result[0].folders).isEmpty()
        assertThat(result[1].id).isEqualTo(FolderId("child"))
        assertThat(result[1].folders).isEmpty()
    }

    @Test
    fun `build handles deep nesting`() {
        // Create a deeply nested folder structure: root -> level1 -> level2 -> ... -> level10
        val folders = mutableListOf<proton.android.pass.domain.Folder>()

        folders.add(
            FolderTestFactory.create(
                folderId = FolderId("level0"),
                parentFolderId = null,
                name = "Level 0"
            )
        )

        for (i in 1..10) {
            folders.add(
                FolderTestFactory.create(
                    folderId = FolderId("level$i"),
                    parentFolderId = FolderId("level${i - 1}"),
                    name = "Level $i"
                )
            )
        }

        val result = FolderTreeBuilder.build(folders)

        // Should have one root
        assertThat(result).hasSize(1)
        assertThat(result[0].id).isEqualTo(FolderId("level0"))

        // Verify the full depth
        var current = result[0]
        for (i in 1..10) {
            assertThat(current.folders).hasSize(1)
            current = current.folders[0]
            assertThat(current.id).isEqualTo(FolderId("level$i"))
            assertThat(current.name).isEqualTo("Level $i")
        }

        // Deepest level should have no children
        assertThat(current.folders).isEmpty()
    }

    @Test
    fun `build handles very deep nesting beyond reasonable limits`() {
        // Create an extremely deep structure (50 levels)
        val folders = mutableListOf<proton.android.pass.domain.Folder>()

        folders.add(
            FolderTestFactory.create(
                folderId = FolderId("level0"),
                parentFolderId = null,
                name = "Level 0"
            )
        )

        for (i in 1..50) {
            folders.add(
                FolderTestFactory.create(
                    folderId = FolderId("level$i"),
                    parentFolderId = FolderId("level${i - 1}"),
                    name = "Level $i"
                )
            )
        }

        val result = FolderTreeBuilder.build(folders)

        // Should still build successfully without stack overflow
        assertThat(result).hasSize(1)

        // Count total depth
        val depth = countMaxDepth(result[0])
        assertThat(depth).isEqualTo(51) // 0-50 inclusive
    }

    @Test
    fun `build handles multiple independent trees`() {
        val tree1Root = FolderTestFactory.create(
            folderId = FolderId("tree1-root"),
            parentFolderId = null,
            name = "Tree 1 Root"
        )
        val tree1Child = FolderTestFactory.create(
            folderId = FolderId("tree1-child"),
            parentFolderId = FolderId("tree1-root"),
            name = "Tree 1 Child"
        )
        val tree2Root = FolderTestFactory.create(
            folderId = FolderId("tree2-root"),
            parentFolderId = null,
            name = "Tree 2 Root"
        )
        val tree2Child = FolderTestFactory.create(
            folderId = FolderId("tree2-child"),
            parentFolderId = FolderId("tree2-root"),
            name = "Tree 2 Child"
        )

        val result = FolderTreeBuilder.build(
            listOf(tree1Root, tree1Child, tree2Root, tree2Child)
        )

        assertThat(result).hasSize(2)

        // Verify both trees are independent
        val tree1 = result.first { it.id.id == "tree1-root" }
        val tree2 = result.first { it.id.id == "tree2-root" }

        assertThat(tree1.folders).hasSize(1)
        assertThat(tree1.folders[0].id).isEqualTo(FolderId("tree1-child"))

        assertThat(tree2.folders).hasSize(1)
        assertThat(tree2.folders[0].id).isEqualTo(FolderId("tree2-child"))
    }

    @Test
    fun `build handles complex circular reference with multiple nodes`() {
        // Create a complex cycle: A -> B -> C -> D -> B
        val folderA = FolderTestFactory.create(
            folderId = FolderId("A"),
            parentFolderId = null,
            name = "A"
        )
        val folderB = FolderTestFactory.create(
            folderId = FolderId("B"),
            parentFolderId = FolderId("A"),
            name = "B"
        )
        val folderC = FolderTestFactory.create(
            folderId = FolderId("C"),
            parentFolderId = FolderId("B"),
            name = "C"
        )
        val folderD = FolderTestFactory.create(
            folderId = FolderId("D"),
            parentFolderId = FolderId("C"),
            name = "D"
        )
        val folderE = FolderTestFactory.create(
            folderId = FolderId("E"),
            parentFolderId = FolderId("D"),
            name = "E (points back to B creating cycle)"
        )
        // Create the cycle by having a folder point to B
        val cycleFolder = folderE.copy(parentFolderId = FolderId("B"))

        val result = FolderTreeBuilder.build(
            listOf(folderA, folderB, folderC, folderD, cycleFolder)
        )

        // Should have one root (A)
        assertThat(result).hasSize(1)
        assertThat(result[0].id).isEqualTo(FolderId("A"))

        // Verify no infinite recursion occurred by checking all nodes are visited once
        val allIds = collectAllIds(result)
        assertThat(allIds).containsNoDuplicates()
    }

    @Test
    fun `build handles mixed scenario with cycles orphans and deep nesting`() {
        val normalRoot = FolderTestFactory.create(
            folderId = FolderId("root"),
            parentFolderId = null,
            name = "Normal Root"
        )
        val deepChild1 = FolderTestFactory.create(
            folderId = FolderId("deep1"),
            parentFolderId = FolderId("root"),
            name = "Deep 1"
        )
        val deepChild2 = FolderTestFactory.create(
            folderId = FolderId("deep2"),
            parentFolderId = FolderId("deep1"),
            name = "Deep 2"
        )
        val orphan = FolderTestFactory.create(
            folderId = FolderId("orphan"),
            parentFolderId = FolderId("missing"),
            name = "Orphan"
        )
        val cycle1 = FolderTestFactory.create(
            folderId = FolderId("cycle1"),
            parentFolderId = FolderId("cycle2"),
            name = "Cycle 1"
        )
        val cycle2 = FolderTestFactory.create(
            folderId = FolderId("cycle2"),
            parentFolderId = FolderId("cycle1"),
            name = "Cycle 2"
        )

        val result = FolderTreeBuilder.build(
            listOf(normalRoot, deepChild1, deepChild2, orphan, cycle1, cycle2)
        )

        // Should have 2 roots: normal root and orphan
        // Cyclic folders are excluded entirely by the cycle detection logic
        assertThat(result).hasSize(2)

        val normalTree = result.first { it.id.id == "root" }
        assertThat(normalTree.folders).hasSize(1)
        assertThat(normalTree.folders[0].folders).hasSize(1)
        assertThat(normalTree.folders[0].folders[0].id).isEqualTo(FolderId("deep2"))

        val orphanTree = result.first { it.id.id == "orphan" }
        assertThat(orphanTree.folders).isEmpty()
    }

    // Helper functions
    private fun collectDescendantIds(folder: FolderUiModel): Set<String> {
        val ids = mutableSetOf<String>()
        folder.folders.forEach { child ->
            ids.add(child.id.id)
            ids.addAll(collectDescendantIds(child))
        }
        return ids
    }

    private fun countMaxDepth(folder: FolderUiModel): Int {
        if (folder.folders.isEmpty()) return 1
        return 1 + folder.folders.maxOf { countMaxDepth(it) }
    }

    private fun collectAllIds(folders: List<FolderUiModel>): List<String> {
        val ids = mutableListOf<String>()
        folders.forEach { folder ->
            ids.add(folder.id.id)
            ids.addAll(collectAllIds(folder.folders))
        }
        return ids
    }
}
