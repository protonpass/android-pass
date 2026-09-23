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

package proton.android.pass.features.migrate.confirmvault

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.data.fakes.repositories.FakeBulkMoveToVaultRepository
import proton.android.pass.data.fakes.usecases.FakeMigrateItems
import proton.android.pass.data.fakes.usecases.FakeMigrateVault
import proton.android.pass.data.fakes.usecases.FakeObserveVaultsWithItemCount
import proton.android.pass.data.fakes.usecases.FakeCanCreateItemsInFolder
import proton.android.pass.data.fakes.usecases.folders.FakeDissolveFolder
import proton.android.pass.data.fakes.usecases.folders.FakeMoveAllItemsInFolder
import proton.android.pass.data.fakes.usecases.folders.FakeMoveFolder
import proton.android.pass.data.fakes.usecases.folders.FakeObserveFolderLimits
import proton.android.pass.data.fakes.usecases.folders.FakeMoveItemsInsideShare
import proton.android.pass.data.fakes.usecases.folders.FakeObserveFoldersByParentId
import proton.android.pass.data.fakes.usecases.items.FakeGetMigrationItemsSelection
import proton.android.pass.data.fakes.usecases.securelink.FakeObserveHasAssociatedSecureLinks
import proton.android.pass.data.fakes.usecases.shares.FakeObserveShare
import proton.android.pass.data.api.usecases.folders.FolderLimitsData
import proton.android.pass.domain.Folder
import proton.android.pass.domain.FolderLimits
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.features.migrate.MigrateModeArg
import proton.android.pass.features.migrate.MigrateModeValue
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.notifications.fakes.FakeSnackbarDispatcher
import proton.android.pass.preferences.FakeInternalSettingsRepository
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.SavedStateHandleTestFactory
import proton.android.pass.test.domain.FolderTestFactory
import proton.android.pass.test.domain.VaultTestFactory

internal class MigrateConfirmVaultForMoveFolderViewModelTest {

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var instance: MigrateConfirmVaultViewModel
    private lateinit var observeVaults: FakeObserveVaultsWithItemCount
    private lateinit var observeFolders: FakeObserveFoldersByParentId
    private lateinit var observeFolderLimits: FakeObserveFolderLimits

    @Before
    fun setup() {
        observeVaults = FakeObserveVaultsWithItemCount()
        observeFolders = FakeObserveFoldersByParentId()
        observeFolderLimits = FakeObserveFolderLimits()

        val fakeSnackbar = FakeSnackbarDispatcher()
        val fakeBulkRepo = FakeBulkMoveToVaultRepository()
        instance = MigrateConfirmVaultViewModel(
            migrator = MigrateConfirmVaultMigrator(
                migrateItems = FakeMigrateItems(),
                migrateVault = FakeMigrateVault(),
                moveFolder = FakeMoveFolder(),
                dissolveFolder = FakeDissolveFolder(),
                moveAllItemsInFolder = FakeMoveAllItemsInFolder(),
                moveItemsInsideShare = FakeMoveItemsInsideShare(),
                snackbarDispatcher = fakeSnackbar,
                bulkMoveToVaultRepository = fakeBulkRepo
            ),
            snackbarDispatcher = fakeSnackbar,
            observeVaults = observeVaults,
            bulkMoveToVaultRepository = fakeBulkRepo,
            observeHasAssociatedSecureLinks = FakeObserveHasAssociatedSecureLinks(),
            savedStateHandle = SavedStateHandleTestFactory.create().apply {
                set(CommonNavArgId.ShareId.key, SHARE_ID.id)
                set(MigrateModeArg.key, MigrateModeValue.MoveFolder.name)
                set(CommonOptionalNavArgId.FolderId.key, MOVING_FOLDER_ID.id)
            },
            observeShare = FakeObserveShare(),
            settingsRepository = FakeInternalSettingsRepository(),
            observeFolders = observeFolders,
            getMigrationItemsSelection = FakeGetMigrationItemsSelection(),
            canCreateItemsInFolder = FakeCanCreateItemsInFolder(),
            observeFolderLimits = observeFolderLimits
        )
    }

    @Test
    fun `folder tree is populated from initial observation`() = runTest {
        // Folders must be set BEFORE vaults: UnconfinedTestDispatcher runs flatMapLatest
        // immediately when observeVaults emits, so take(1) fires before folders are set otherwise.
        observeFolders.sendResult(
            userId = USER_ID,
            shareId = SHARE_ID,
            result = Result.success(listOf(childFolder()))
        )
        observeVaults.sendResult(Result.success(listOf(sourceVault())))

        instance.state.test {
            val state = awaitItem()
            assertThat(state.vaultList).hasSize(1)
            assertThat(state.vaultList[0].folderTree).isNotEmpty()
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `folder tree does not update when DB emits after move due to take(1)`() = runTest {
        // Folders before vaults: see comment in folder tree test above.
        observeFolders.sendResult(
            userId = USER_ID,
            shareId = SHARE_ID,
            result = Result.success(listOf(childFolder()))
        )
        observeVaults.sendResult(Result.success(listOf(sourceVault())))

        instance.state.test {
            val state = awaitItem()
            assertThat(state.vaultList).hasSize(1)
            val initialTree = state.vaultList[0].folderTree
            assertThat(initialTree).isNotEmpty()

            // Simulate DB update triggered by the move operation completing.
            // take(1) already completed the subscription so this should not propagate.
            observeFolders.sendResult(
                userId = USER_ID,
                shareId = SHARE_ID,
                result = Result.success(emptyList())
            )

            expectNoEvents()
            assertThat(instance.state.value.vaultList[0].folderTree).isEqualTo(initialTree)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `direct child of moving folder appears in disabledDescendantFolderIds`() = runTest {
        observeFolders.sendResult(
            userId = USER_ID,
            shareId = SHARE_ID,
            result = Result.success(listOf(childFolder()))
        )
        observeVaults.sendResult(Result.success(listOf(sourceVault())))

        instance.state.test {
            val state = awaitItem()
            assertThat(state.disabledDescendantFolderIds).contains(CHILD_FOLDER_ID)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `grandchild of moving folder appears in disabledDescendantFolderIds`() = runTest {
        observeFolders.sendResult(
            userId = USER_ID,
            shareId = SHARE_ID,
            result = Result.success(listOf(childFolder(), grandchildFolder()))
        )
        observeVaults.sendResult(Result.success(listOf(sourceVault())))

        instance.state.test {
            val state = awaitItem()
            assertThat(state.disabledDescendantFolderIds).containsAtLeast(CHILD_FOLDER_ID, GRANDCHILD_FOLDER_ID)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `selecting a direct child folder is ignored`() = runTest {
        observeFolders.sendResult(
            userId = USER_ID,
            shareId = SHARE_ID,
            result = Result.success(listOf(childFolder()))
        )
        observeVaults.sendResult(Result.success(listOf(sourceVault())))

        instance.state.test {
            // Subscribe first so descendantFolderIdsFlow becomes active before the guard check.
            awaitItem()
            instance.onFolderSelected(SHARE_ID, CHILD_FOLDER_ID)
            expectNoEvents()
            assertThat(instance.state.value.selectedFolderId).isEqualTo(proton.android.pass.common.api.None)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `selecting a nested descendant folder is ignored`() = runTest {
        observeFolders.sendResult(
            userId = USER_ID,
            shareId = SHARE_ID,
            result = Result.success(listOf(childFolder(), grandchildFolder()))
        )
        observeVaults.sendResult(Result.success(listOf(sourceVault())))

        instance.state.test {
            // Subscribe first so descendantFolderIdsFlow becomes active before the guard check.
            awaitItem()
            instance.onFolderSelected(SHARE_ID, GRANDCHILD_FOLDER_ID)
            expectNoEvents()
            assertThat(instance.state.value.selectedFolderId).isEqualTo(proton.android.pass.common.api.None)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `moving folder to folder target sets selectedFolderId`() = runTest {
        observeFolders.sendResult(
            userId = USER_ID,
            shareId = SHARE_ID,
            result = Result.success(listOf(childFolder()))
        )
        observeVaults.sendResult(Result.success(listOf(sourceVault())))

        instance.onFolderSelected(SHARE_ID, DESTINATION_FOLDER_ID)

        instance.state.test {
            val state = awaitItem()
            assertThat(state.selectedFolderId).isEqualTo(proton.android.pass.common.api.Some(DESTINATION_FOLDER_ID))
        }
    }

    @Test
    fun `selecting vault root when folder is already at root shows dissolve dialog`() = runTest {
        // MOVING_FOLDER_ID has no parentFolderId → it is at vault root.
        // Folders before vaults: see comment in folder tree test above.
        observeFolders.sendResult(
            userId = USER_ID,
            shareId = SHARE_ID,
            result = Result.success(
                listOf(
                    FolderTestFactory.create(
                        userId = USER_ID,
                        shareId = SHARE_ID,
                        folderId = MOVING_FOLDER_ID,
                        parentFolderId = null // root
                    )
                )
            )
        )
        observeVaults.sendResult(Result.success(listOf(sourceVault())))

        instance.onVaultSelected(SHARE_ID)

        instance.state.test {
            val state = awaitItem()
            assertThat(state.showDissolveFolderDialog).isTrue()
        }
    }

    @Test
    fun `folders where moving subtree would exceed max depth appear in limitExceededFolderIds`() = runTest {
        observeFolders.sendResult(
            userId = USER_ID,
            shareId = SHARE_ID,
            result = Result.success(listOf(movingFolder(), childFolder()) + chainFolders(depth = 4))
        )
        observeVaults.sendResult(Result.success(listOf(sourceVault())))

        instance.state.test {
            val state = awaitItem()
            assertThat(state.limitExceededFolderIds).containsExactly(chainFolderId(4))
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `folder at max children appears in limitExceededFolderIds`() = runTest {
        observeFolderLimits.sendValue(
            FolderLimitsData(
                maxCount = FolderLimits.MAX_FOLDERS_PER_VAULT,
                maxChildren = 1,
                maxDepth = FolderLimits.MAX_FOLDER_DEPTH
            )
        )
        observeFolders.sendResult(
            userId = USER_ID,
            shareId = SHARE_ID,
            result = Result.success(listOf(movingFolder()) + chainFolders(depth = 2))
        )
        observeVaults.sendResult(Result.success(listOf(sourceVault())))

        instance.state.test {
            val state = awaitItem()
            assertThat(state.limitExceededFolderIds).containsExactly(chainFolderId(1))
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `selecting a folder exceeding limits is ignored`() = runTest {
        observeFolders.sendResult(
            userId = USER_ID,
            shareId = SHARE_ID,
            result = Result.success(listOf(movingFolder(), childFolder()) + chainFolders(depth = 4))
        )
        observeVaults.sendResult(Result.success(listOf(sourceVault())))

        instance.state.test {
            awaitItem()
            instance.onFolderSelected(SHARE_ID, chainFolderId(4))
            expectNoEvents()
            assertThat(instance.state.value.selectedFolderId).isEqualTo(proton.android.pass.common.api.None)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `vault root at max children disables vault row for nested folder`() = runTest {
        observeFolderLimits.sendValue(singleChildLimits())
        observeFolders.sendResult(
            userId = USER_ID,
            shareId = SHARE_ID,
            result = Result.success(listOf(nestedMovingFolder()) + chainFolders(depth = 1))
        )
        observeVaults.sendResult(Result.success(listOf(sourceVault())))

        instance.state.test {
            val state = awaitItem()
            assertThat(state.vaultList.single().status)
                .isEqualTo(VaultStatus.Disabled(VaultStatus.DisabledReason.FolderLimitReached))
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `selecting vault root at max children is ignored`() = runTest {
        observeFolderLimits.sendValue(singleChildLimits())
        observeFolders.sendResult(
            userId = USER_ID,
            shareId = SHARE_ID,
            result = Result.success(listOf(nestedMovingFolder()) + chainFolders(depth = 1))
        )
        observeVaults.sendResult(Result.success(listOf(sourceVault())))

        instance.state.test {
            awaitItem()
            instance.onVaultSelected(SHARE_ID)
            expectNoEvents()
            assertThat(instance.state.value.selectedShareId).isEqualTo(proton.android.pass.common.api.None)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `vault root at max children stays enabled when folder is already at root`() = runTest {
        observeFolderLimits.sendValue(singleChildLimits())
        observeFolders.sendResult(
            userId = USER_ID,
            shareId = SHARE_ID,
            result = Result.success(listOf(movingFolder()))
        )
        observeVaults.sendResult(Result.success(listOf(sourceVault())))

        instance.state.test {
            val state = awaitItem()
            assertThat(state.vaultList.single().status).isEqualTo(VaultStatus.Enabled)
            cancelAndConsumeRemainingEvents()
        }
    }

    private fun singleChildLimits() = FolderLimitsData(
        maxCount = FolderLimits.MAX_FOLDERS_PER_VAULT,
        maxChildren = 1,
        maxDepth = FolderLimits.MAX_FOLDER_DEPTH
    )

    private fun nestedMovingFolder() = FolderTestFactory.create(
        userId = USER_ID,
        shareId = SHARE_ID,
        folderId = MOVING_FOLDER_ID,
        parentFolderId = chainFolderId(1)
    )

    private fun movingFolder() = FolderTestFactory.create(
        userId = USER_ID,
        shareId = SHARE_ID,
        folderId = MOVING_FOLDER_ID,
        parentFolderId = null
    )

    private fun chainFolderId(level: Int) = FolderId("chain-folder-$level")

    private fun chainFolders(depth: Int): List<Folder> = (1..depth).map { level ->
        FolderTestFactory.create(
            userId = USER_ID,
            shareId = SHARE_ID,
            folderId = chainFolderId(level),
            parentFolderId = if (level == 1) null else chainFolderId(level - 1)
        )
    }

    private fun sourceVault(): VaultWithItemCount = VaultWithItemCount(
        vault = VaultTestFactory.create(userId = USER_ID, shareId = SHARE_ID),
        activeItemCount = 1,
        trashedItemCount = 0
    )

    private fun childFolder() = FolderTestFactory.create(
        userId = USER_ID,
        shareId = SHARE_ID,
        folderId = CHILD_FOLDER_ID,
        parentFolderId = MOVING_FOLDER_ID
    )

    private fun grandchildFolder() = FolderTestFactory.create(
        userId = USER_ID,
        shareId = SHARE_ID,
        folderId = GRANDCHILD_FOLDER_ID,
        parentFolderId = CHILD_FOLDER_ID
    )

    companion object {
        private val USER_ID = UserId("789") // matches VaultTestFactory default
        private val SHARE_ID = ShareId("share-1")
        private val MOVING_FOLDER_ID = FolderId("moving-folder")
        private val CHILD_FOLDER_ID = FolderId("child-folder")
        private val GRANDCHILD_FOLDER_ID = FolderId("grandchild-folder")
        private val DESTINATION_FOLDER_ID = FolderId("destination-folder")
    }
}
