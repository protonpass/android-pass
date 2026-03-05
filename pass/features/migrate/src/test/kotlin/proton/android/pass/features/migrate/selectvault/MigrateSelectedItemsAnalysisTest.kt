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

package proton.android.pass.features.migrate.selectvault

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import proton.android.pass.common.api.None
import proton.android.pass.common.api.toOption
import proton.android.pass.data.api.repositories.ParentContainer
import proton.android.pass.data.api.repositories.toBulkMoveToVaultSelection
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId

class MigrateSelectedItemsAnalysisTest {

    @Test
    fun `single share with only root items disables source vault`() {
        val selection = mapOf(
            SHARE_ID to listOf(ITEM_ID_1, ITEM_ID_2)
        ).toBulkMoveToVaultSelection()

        val analysis = analyzeSelectedItems(selection)

        assertThat(analysis.sourceShareId).isEqualTo(SHARE_ID)
        assertThat(analysis.disableSourceVault).isTrue()
        assertThat(analysis.disabledFolderId).isEqualTo(None)
        assertThat(analysis.disabledFolderItemCount).isEqualTo(0)
    }

    @Test
    fun `single share with mixed root and folder keeps source vault enabled`() {
        val selection = mapOf(
            SHARE_ID to mapOf<ParentContainer, Set<ItemId>>(
                ParentContainer.Share to setOf(ITEM_ID_1),
                ParentContainer.Folder(FOLDER_ID_1) to setOf(ITEM_ID_2)
            )
        )

        val analysis = analyzeSelectedItems(selection)

        assertThat(analysis.sourceShareId).isEqualTo(SHARE_ID)
        assertThat(analysis.disableSourceVault).isFalse()
        assertThat(analysis.disabledFolderId).isEqualTo(None)
        assertThat(analysis.disabledFolderItemCount).isEqualTo(0)
    }

    @Test
    fun `single share with one folder disables that folder with total count`() {
        val selection = mapOf(
            SHARE_ID to mapOf<ParentContainer, Set<ItemId>>(
                ParentContainer.Folder(FOLDER_ID_1) to setOf(ITEM_ID_1, ITEM_ID_2)
            )
        )

        val analysis = analyzeSelectedItems(selection)

        assertThat(analysis.sourceShareId).isEqualTo(SHARE_ID)
        assertThat(analysis.disableSourceVault).isFalse()
        assertThat(analysis.disabledFolderId).isEqualTo(FOLDER_ID_1.toOption())
        assertThat(analysis.disabledFolderItemCount).isEqualTo(2)
    }

    @Test
    fun `single share with multiple folders disables no folder`() {
        val selection = mapOf(
            SHARE_ID to mapOf<ParentContainer, Set<ItemId>>(
                ParentContainer.Folder(FOLDER_ID_1) to setOf(ITEM_ID_1),
                ParentContainer.Folder(FOLDER_ID_2) to setOf(ITEM_ID_2)
            )
        )

        val analysis = analyzeSelectedItems(selection)

        assertThat(analysis.sourceShareId).isEqualTo(SHARE_ID)
        assertThat(analysis.disableSourceVault).isFalse()
        assertThat(analysis.disabledFolderId).isEqualTo(None)
        assertThat(analysis.disabledFolderItemCount).isEqualTo(0)
    }

    @Test
    fun `multiple shares yields empty analysis`() {
        val selection = mapOf(
            SHARE_ID to listOf(ITEM_ID_1),
            ShareId("share-2") to listOf(ITEM_ID_2)
        ).toBulkMoveToVaultSelection()

        val analysis = analyzeSelectedItems(selection)

        assertThat(analysis).isEqualTo(SelectedItemsAnalysis.Empty)
    }

    private companion object {
        private val SHARE_ID = ShareId("share-1")
        private val FOLDER_ID_1 = FolderId("folder-1")
        private val FOLDER_ID_2 = FolderId("folder-2")
        private val ITEM_ID_1 = ItemId("item-1")
        private val ITEM_ID_2 = ItemId("item-2")
    }
}
