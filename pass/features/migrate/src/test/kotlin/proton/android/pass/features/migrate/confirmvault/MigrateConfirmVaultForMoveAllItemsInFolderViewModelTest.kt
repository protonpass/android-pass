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
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.toOption
import proton.android.pass.data.fakes.repositories.FakeBulkMoveToVaultRepository
import proton.android.pass.data.fakes.usecases.FakeMigrateItems
import proton.android.pass.data.fakes.usecases.FakeMigrateVault
import proton.android.pass.data.fakes.usecases.FakeObserveVaultsWithItemCount
import proton.android.pass.data.fakes.usecases.folders.FakeDissolveFolder
import proton.android.pass.data.fakes.usecases.folders.FakeMoveAllItemsInFolder
import proton.android.pass.data.fakes.usecases.folders.FakeMoveFolder
import proton.android.pass.data.fakes.usecases.folders.FakeMoveItemsInsideShare
import proton.android.pass.data.fakes.usecases.folders.FakeObserveFoldersByParentId
import proton.android.pass.data.fakes.usecases.items.FakeGetMigrationItemsSelection
import proton.android.pass.data.fakes.usecases.securelink.FakeObserveHasAssociatedSecureLinks
import proton.android.pass.data.fakes.usecases.shares.FakeObserveShare
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.features.migrate.MigrateModeArg
import proton.android.pass.features.migrate.MigrateModeValue
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.notifications.fakes.FakeSnackbarDispatcher
import proton.android.pass.preferences.FakeInternalSettingsRepository
import proton.android.pass.domain.items.MigrationItemsSelection
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.SavedStateHandleTestFactory
import proton.android.pass.test.domain.ItemTestFactory
import proton.android.pass.test.domain.VaultTestFactory

internal class MigrateConfirmVaultForMoveAllItemsInFolderViewModelTest {

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var instance: MigrateConfirmVaultViewModel
    private lateinit var observeVaults: FakeObserveVaultsWithItemCount
    private lateinit var observeFolders: FakeObserveFoldersByParentId

    @Before
    fun setup() {
        observeVaults = FakeObserveVaultsWithItemCount()
        observeFolders = FakeObserveFoldersByParentId()
        instance = buildInstance()
    }

    @Test
    fun `source folder with no child folders is disabled`() = runTest {
        // No folders → source folder stays disabled
        observeVaults.sendResult(Result.success(listOf(sourceVault())))

        instance.state.test {
            val state = awaitItem()
            assertThat(state.disabledFolderId).isEqualTo(SOURCE_FOLDER_ID.toOption())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `source folder with items in descendant folders is not disabled`() = runTest {
        val testInstance = buildInstance(
            FakeGetMigrationItemsSelection().apply {
                setMigrationItemsSelection(
                    MigrationItemsSelection(
                        items = listOf(ItemTestFactory.create(shareId = SHARE_ID, folderId = CHILD_FOLDER_ID))
                    )
                )
            }
        )
        observeVaults.sendResult(Result.success(listOf(sourceVault())))

        testInstance.state.test {
            val state = awaitItem()
            assertThat(state.disabledFolderId).isEqualTo(None)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `source folder with items in descendant folders can be selected as destination`() = runTest {
        val testInstance = buildInstance(
            FakeGetMigrationItemsSelection().apply {
                setMigrationItemsSelection(
                    MigrationItemsSelection(
                        items = listOf(ItemTestFactory.create(shareId = SHARE_ID, folderId = CHILD_FOLDER_ID))
                    )
                )
            }
        )
        observeVaults.sendResult(Result.success(listOf(sourceVault())))

        testInstance.state.test {
            awaitItem() // activate state so sourceHasChildFolders is computed
            testInstance.onFolderSelected(SHARE_ID, SOURCE_FOLDER_ID)
            val state = awaitItem()
            assertThat(state.selectedFolderId).isEqualTo(Some(SOURCE_FOLDER_ID))
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `source folder without children cannot be selected as destination`() = runTest {
        // With no children the source folder is disabled; the ViewModel guard blocks selection too.
        observeVaults.sendResult(Result.success(listOf(sourceVault())))

        instance.state.test {
            awaitItem() // activate state so sourceHasChildFolders = false is known
            instance.onFolderSelected(SHARE_ID, SOURCE_FOLDER_ID)
            expectNoEvents()
            assertThat(instance.state.value.selectedFolderId).isEqualTo(None)
            cancelAndConsumeRemainingEvents()
        }
    }

    private fun buildInstance(
        getMigrationItemsSelection: FakeGetMigrationItemsSelection = FakeGetMigrationItemsSelection()
    ): MigrateConfirmVaultViewModel {
        val fakeSnackbar = FakeSnackbarDispatcher()
        val fakeBulkRepo = FakeBulkMoveToVaultRepository()
        return MigrateConfirmVaultViewModel(
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
                set(MigrateModeArg.key, MigrateModeValue.MoveAllItemsInFolder.name)
                set(CommonOptionalNavArgId.FolderId.key, SOURCE_FOLDER_ID.id)
            },
            observeShare = FakeObserveShare(),
            settingsRepository = FakeInternalSettingsRepository(),
            observeFolders = observeFolders,
            getMigrationItemsSelection = getMigrationItemsSelection
        )
    }

    private fun sourceVault(): VaultWithItemCount = VaultWithItemCount(
        vault = VaultTestFactory.create(userId = USER_ID, shareId = SHARE_ID),
        activeItemCount = 1,
        trashedItemCount = 0
    )

    companion object {
        private val USER_ID = UserId("789") // matches VaultTestFactory default
        private val SHARE_ID = ShareId("share-1")
        private val SOURCE_FOLDER_ID = FolderId("source-folder")
        private val CHILD_FOLDER_ID = FolderId("child-folder")
    }
}
