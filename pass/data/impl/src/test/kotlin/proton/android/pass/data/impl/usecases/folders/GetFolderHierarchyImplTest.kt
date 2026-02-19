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

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Test
import proton.android.pass.data.fakes.usecases.FakeGetItemById
import proton.android.pass.data.impl.fakes.FakeFolderRepository
import proton.android.pass.domain.Folder
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.test.domain.ItemTestFactory

internal class GetFolderHierarchyImplTest {

    private lateinit var getItemById: FakeGetItemById
    private lateinit var folderRepository: FakeFolderRepository
    private lateinit var instance: GetFolderHierarchyImpl

    @Before
    fun setup() {
        getItemById = FakeGetItemById()
        folderRepository = FakeFolderRepository()
        instance = GetFolderHierarchyImpl(
            getItemById = getItemById,
            folderRepository = folderRepository
        )
    }

    @Test
    fun `delegates to repository with current user and returns hierarchy`() = runTest {
        val expected = emptyList<Folder>()
        folderRepository.getFolderHierarchyResult = expected
        getItemById.emit(
            shareId = SHARE_ID,
            itemId = ITEM_ID,
            value = Result.success(
                ItemTestFactory.create(
                    shareId = SHARE_ID,
                    itemId = ITEM_ID
                ).copy(
                    userId = USER_ID,
                    folderId = FOLDER_ID
                )
            )
        )

        val result = instance.invoke(
            shareId = SHARE_ID,
            itemId = ITEM_ID
        )

        assertThat(result).isEqualTo(expected)
        assertThat(folderRepository.lastGetFolderHierarchyCall).isEqualTo(
            FakeFolderRepository.GetFolderHierarchyCall(
                userId = USER_ID,
                shareId = SHARE_ID,
                folderId = FOLDER_ID
            )
        )
    }

    @Test
    fun `returns empty breadcrumbs when item has no folder`() = runTest {
        getItemById.emit(
            shareId = SHARE_ID,
            itemId = ITEM_ID,
            value = Result.success(
                ItemTestFactory.create(
                    shareId = SHARE_ID,
                    itemId = ITEM_ID
                ).copy(userId = USER_ID, folderId = null)
            )
        )

        val result = instance.invoke(
            shareId = SHARE_ID,
            itemId = ITEM_ID
        )

        assertThat(result).isEmpty()
        assertThat(folderRepository.lastGetFolderHierarchyCall).isNull()
    }

    private companion object {
        private val USER_ID = UserId("user-id")
        private val SHARE_ID = ShareId("share-id")
        private val ITEM_ID = ItemId("item-id")
        private val FOLDER_ID = FolderId("leaf")
    }
}
