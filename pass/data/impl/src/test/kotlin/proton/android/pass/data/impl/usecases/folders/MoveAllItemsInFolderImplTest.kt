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
import org.junit.Before
import org.junit.Test
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.data.api.repositories.MigrateItemsResult
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.fakes.usecases.FakeMigrateItems
import proton.android.pass.data.fakes.usecases.FakeObserveItems
import proton.android.pass.data.fakes.usecases.folders.FakeMoveItemsInsideShare
import proton.android.pass.data.fakes.usecases.folders.FakeObserveFoldersByParentId
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareSelection
import proton.android.pass.test.domain.FolderTestFactory
import proton.android.pass.test.domain.ItemTestFactory

internal class MoveAllItemsInFolderImplTest {

    private lateinit var observeFolders: FakeObserveFoldersByParentId
    private lateinit var observeItems: FakeObserveItems
    private lateinit var moveItemsInsideShare: FakeMoveItemsInsideShare
    private lateinit var migrateItems: FakeMigrateItems
    private lateinit var instance: MoveAllItemsInFolderImpl

    @Before
    fun setup() {
        observeFolders = FakeObserveFoldersByParentId()
        observeItems = FakeObserveItems()
        moveItemsInsideShare = FakeMoveItemsInsideShare()
        migrateItems = FakeMigrateItems()
        instance = MoveAllItemsInFolderImpl(
            observeFoldersByParentId = observeFolders,
            observeItems = observeItems,
            moveItemsInsideShare = moveItemsInsideShare,
            migrateItems = migrateItems
        )
    }

    @Test
    fun `empty folder with no children does not call move or migrate`() = runTest {
        observeFolders.sendResult(SHARE_ID, ROOT_FOLDER_ID, Result.success(emptyList()))
        observeItems.emit(itemParams(ROOT_FOLDER_ID), emptyList())

        instance(SHARE_ID, ROOT_FOLDER_ID, SHARE_ID, null)

        assertThat(moveItemsInsideShare.invocations).isEmpty()
        assertThat(migrateItems.memory()).isEmpty()
    }

    @Test
    fun `flat folder with items same vault calls moveItemsInsideShare`() = runTest {
        val item1 = ItemTestFactory.create(shareId = SHARE_ID, itemId = ItemId("item-1"))
        val item2 = ItemTestFactory.create(shareId = SHARE_ID, itemId = ItemId("item-2"))
        observeFolders.sendResult(SHARE_ID, ROOT_FOLDER_ID, Result.success(emptyList()))
        observeItems.emit(itemParams(ROOT_FOLDER_ID), listOf(item1, item2))

        instance(SHARE_ID, ROOT_FOLDER_ID, SHARE_ID, DEST_FOLDER_ID)

        assertThat(moveItemsInsideShare.invocations).hasSize(1)
        val invocation = moveItemsInsideShare.invocations[0]
        assertThat(invocation.shareId).isEqualTo(SHARE_ID)
        assertThat(invocation.folderId).isEqualTo(DEST_FOLDER_ID)
        assertThat(invocation.itemIds).containsExactly(item1.id, item2.id)
        assertThat(migrateItems.memory()).isEmpty()
    }

    @Test
    fun `flat folder with items cross-vault calls migrateItems`() = runTest {
        val item1 = ItemTestFactory.create(shareId = SHARE_ID, itemId = ItemId("item-1"))
        observeFolders.sendResult(SHARE_ID, ROOT_FOLDER_ID, Result.success(emptyList()))
        observeItems.emit(itemParams(ROOT_FOLDER_ID), listOf(item1))
        migrateItems.setResult(Result.success(MigrateItemsResult.AllMigrated(listOf(item1))))

        instance(SHARE_ID, ROOT_FOLDER_ID, DEST_SHARE_ID, DEST_FOLDER_ID)

        assertThat(moveItemsInsideShare.invocations).isEmpty()
        assertThat(migrateItems.memory()).hasSize(1)
        val payload = migrateItems.memory()[0]
        assertThat(payload.items).isEqualTo(mapOf(SHARE_ID to listOf(item1.id)))
        assertThat(payload.destinationShare).isEqualTo(DEST_SHARE_ID)
        assertThat(payload.destinationFolderId).isEqualTo(Some(DEST_FOLDER_ID))
    }

    @Test
    fun `nested folder root + child both with items collects all item ids`() = runTest {
        val child = FolderTestFactory.create(
            shareId = SHARE_ID,
            folderId = CHILD_FOLDER_ID,
            parentFolderId = ROOT_FOLDER_ID
        )
        val rootItem = ItemTestFactory.create(shareId = SHARE_ID, itemId = ItemId("root-item"))
        val childItem = ItemTestFactory.create(shareId = SHARE_ID, itemId = ItemId("child-item"))

        observeFolders.sendResult(SHARE_ID, ROOT_FOLDER_ID, Result.success(listOf(child)))
        observeFolders.sendResult(SHARE_ID, CHILD_FOLDER_ID, Result.success(emptyList()))
        observeItems.emit(itemParams(ROOT_FOLDER_ID), listOf(rootItem))
        observeItems.emit(itemParams(CHILD_FOLDER_ID), listOf(childItem))

        instance(SHARE_ID, ROOT_FOLDER_ID, SHARE_ID, DEST_FOLDER_ID)

        assertThat(moveItemsInsideShare.invocations).hasSize(1)
        assertThat(moveItemsInsideShare.invocations[0].itemIds)
            .containsExactly(rootItem.id, childItem.id)
    }

    @Test
    fun `deep nesting three levels all items collected`() = runTest {
        val grandchild = FolderTestFactory.create(
            shareId = SHARE_ID,
            folderId = GRANDCHILD_FOLDER_ID,
            parentFolderId = CHILD_FOLDER_ID
        )
        val child = FolderTestFactory.create(
            shareId = SHARE_ID,
            folderId = CHILD_FOLDER_ID,
            parentFolderId = ROOT_FOLDER_ID
        )
        val rootItem = ItemTestFactory.create(shareId = SHARE_ID, itemId = ItemId("root-item"))
        val childItem = ItemTestFactory.create(shareId = SHARE_ID, itemId = ItemId("child-item"))
        val grandchildItem = ItemTestFactory.create(shareId = SHARE_ID, itemId = ItemId("grandchild-item"))

        observeFolders.sendResult(SHARE_ID, ROOT_FOLDER_ID, Result.success(listOf(child)))
        observeFolders.sendResult(SHARE_ID, CHILD_FOLDER_ID, Result.success(listOf(grandchild)))
        observeFolders.sendResult(SHARE_ID, GRANDCHILD_FOLDER_ID, Result.success(emptyList()))
        observeItems.emit(itemParams(ROOT_FOLDER_ID), listOf(rootItem))
        observeItems.emit(itemParams(CHILD_FOLDER_ID), listOf(childItem))
        observeItems.emit(itemParams(GRANDCHILD_FOLDER_ID), listOf(grandchildItem))

        instance(SHARE_ID, ROOT_FOLDER_ID, SHARE_ID, DEST_FOLDER_ID)

        assertThat(moveItemsInsideShare.invocations).hasSize(1)
        assertThat(moveItemsInsideShare.invocations[0].itemIds)
            .containsExactly(rootItem.id, childItem.id, grandchildItem.id)
    }

    @Test
    fun `empty root but child has items still collects child items`() = runTest {
        val child = FolderTestFactory.create(
            shareId = SHARE_ID,
            folderId = CHILD_FOLDER_ID,
            parentFolderId = ROOT_FOLDER_ID
        )
        val childItem = ItemTestFactory.create(shareId = SHARE_ID, itemId = ItemId("child-item"))

        observeFolders.sendResult(SHARE_ID, ROOT_FOLDER_ID, Result.success(listOf(child)))
        observeFolders.sendResult(SHARE_ID, CHILD_FOLDER_ID, Result.success(emptyList()))
        observeItems.emit(itemParams(ROOT_FOLDER_ID), emptyList())
        observeItems.emit(itemParams(CHILD_FOLDER_ID), listOf(childItem))

        instance(SHARE_ID, ROOT_FOLDER_ID, SHARE_ID, DEST_FOLDER_ID)

        assertThat(moveItemsInsideShare.invocations).hasSize(1)
        assertThat(moveItemsInsideShare.invocations[0].itemIds).containsExactly(childItem.id)
    }

    @Test
    fun `cross-vault with null destFolderId passes None to migrateItems`() = runTest {
        val item = ItemTestFactory.create(shareId = SHARE_ID, itemId = ItemId("item-1"))
        observeFolders.sendResult(SHARE_ID, ROOT_FOLDER_ID, Result.success(emptyList()))
        observeItems.emit(itemParams(ROOT_FOLDER_ID), listOf(item))
        migrateItems.setResult(Result.success(MigrateItemsResult.AllMigrated(listOf(item))))

        instance(SHARE_ID, ROOT_FOLDER_ID, DEST_SHARE_ID, null)

        assertThat(migrateItems.memory()[0].destinationFolderId).isEqualTo(None)
    }

    @Test
    fun `same vault with null destFolderId passes null to moveItemsInsideShare`() = runTest {
        val item = ItemTestFactory.create(shareId = SHARE_ID, itemId = ItemId("item-1"))
        observeFolders.sendResult(SHARE_ID, ROOT_FOLDER_ID, Result.success(emptyList()))
        observeItems.emit(itemParams(ROOT_FOLDER_ID), listOf(item))

        instance(SHARE_ID, ROOT_FOLDER_ID, SHARE_ID, null)

        assertThat(moveItemsInsideShare.invocations[0].folderId).isNull()
    }

    @Test
    fun `cross-vault NoneMigrated throws the contained exception`() = runTest {
        val item = ItemTestFactory.create(shareId = SHARE_ID, itemId = ItemId("item-1"))
        val cause = RuntimeException("migration failed")
        observeFolders.sendResult(SHARE_ID, ROOT_FOLDER_ID, Result.success(emptyList()))
        observeItems.emit(itemParams(ROOT_FOLDER_ID), listOf(item))
        migrateItems.setResult(Result.success(MigrateItemsResult.NoneMigrated(cause)))

        val thrown = runCatching {
            instance(SHARE_ID, ROOT_FOLDER_ID, DEST_SHARE_ID, null)
        }.exceptionOrNull()

        assertThat(thrown).isEqualTo(cause)
    }

    @Test
    fun `cross-vault SomeMigrated does not throw`() = runTest {
        val item = ItemTestFactory.create(shareId = SHARE_ID, itemId = ItemId("item-1"))
        observeFolders.sendResult(SHARE_ID, ROOT_FOLDER_ID, Result.success(emptyList()))
        observeItems.emit(itemParams(ROOT_FOLDER_ID), listOf(item))
        migrateItems.setResult(Result.success(MigrateItemsResult.SomeMigrated(listOf(item))))

        instance(SHARE_ID, ROOT_FOLDER_ID, DEST_SHARE_ID, null)

        assertThat(migrateItems.memory()).hasSize(1)
    }

    private fun itemParams(folderId: FolderId) = FakeObserveItems.Params(
        selection = ShareSelection.Folder(SHARE_ID, folderId),
        itemState = ItemState.Active,
        filter = ItemTypeFilter.All
    )

    private companion object {
        val SHARE_ID = ShareId("share-1")
        val DEST_SHARE_ID = ShareId("share-2")
        val ROOT_FOLDER_ID = FolderId("root-folder")
        val CHILD_FOLDER_ID = FolderId("child-folder")
        val GRANDCHILD_FOLDER_ID = FolderId("grandchild-folder")
        val DEST_FOLDER_ID = FolderId("dest-folder")
    }
}
