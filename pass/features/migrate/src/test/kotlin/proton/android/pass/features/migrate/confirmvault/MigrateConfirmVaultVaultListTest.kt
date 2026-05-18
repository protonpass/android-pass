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
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.common.api.None
import proton.android.pass.common.api.toOption
import proton.android.pass.data.api.repositories.ParentContainer
import proton.android.pass.data.api.repositories.toBulkMoveToVaultSelection
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
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.features.migrate.MigrateModeArg
import proton.android.pass.features.migrate.MigrateModeValue
import proton.android.pass.features.migrate.MigrateVaultFilter
import proton.android.pass.features.migrate.MigrateVaultFilterArg
import proton.android.pass.notifications.fakes.FakeSnackbarDispatcher
import proton.android.pass.preferences.FakeInternalSettingsRepository
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.SavedStateHandleTestFactory
import proton.android.pass.test.domain.VaultTestFactory

/**
 * Tests for vault list population, disabling logic, folder analysis, and filter modes
 * in MigrateConfirmVaultViewModel when operating in SelectedItems mode.
 */
class MigrateConfirmVaultVaultListTest {

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var observeVaults: FakeObserveVaultsWithItemCount
    private lateinit var observeFolders: FakeObserveFoldersByParentId
    private lateinit var snackbarDispatcher: FakeSnackbarDispatcher
    private lateinit var bulkMoveToVaultRepository: FakeBulkMoveToVaultRepository

    @Before
    fun setup() {
        observeVaults = FakeObserveVaultsWithItemCount()
        observeFolders = FakeObserveFoldersByParentId()
        snackbarDispatcher = FakeSnackbarDispatcher()
        bulkMoveToVaultRepository = FakeBulkMoveToVaultRepository().apply {
            runBlocking { save(mapOf(SHARE_ID to listOf(ITEM_ID)).toBulkMoveToVaultSelection()) }
        }
    }

    private fun buildInstance(
        bulkRepo: FakeBulkMoveToVaultRepository = bulkMoveToVaultRepository,
        filter: MigrateVaultFilter = MigrateVaultFilter.All
    ): MigrateConfirmVaultViewModel = MigrateConfirmVaultViewModel(
        migrator = MigrateConfirmVaultMigrator(
            migrateItems = FakeMigrateItems(),
            migrateVault = FakeMigrateVault(),
            moveFolder = FakeMoveFolder(),
            dissolveFolder = FakeDissolveFolder(),
            moveAllItemsInFolder = FakeMoveAllItemsInFolder(),
            moveItemsInsideShare = FakeMoveItemsInsideShare(),
            snackbarDispatcher = snackbarDispatcher,
            bulkMoveToVaultRepository = bulkRepo
        ),
        snackbarDispatcher = snackbarDispatcher,
        observeVaults = observeVaults,
        bulkMoveToVaultRepository = bulkRepo,
        observeHasAssociatedSecureLinks = FakeObserveHasAssociatedSecureLinks(),
        savedStateHandle = SavedStateHandleTestFactory.create().apply {
            set(MigrateVaultFilterArg.key, filter.name)
            set(MigrateModeArg.key, MigrateModeValue.SelectedItems.name)
        },
        observeShare = FakeObserveShare(),
        settingsRepository = FakeInternalSettingsRepository(),
        observeFolders = observeFolders,
        getMigrationItemsSelection = FakeGetMigrationItemsSelection()
    )

    @Test
    fun `marks the current vault as disabled when all items are at root level`() = runTest {
        val (currentVault, otherVault) = initialVaults()
        observeVaults.sendResult(Result.success(listOf(currentVault, otherVault)))

        buildInstance().state.test {
            val state = awaitItem()
            val currentState = state.vaultList.find { it.vaultWithItemCount.vault.shareId == SHARE_ID }
            assertThat(currentState).isNotNull()
            assertThat(currentState!!.status).isEqualTo(VaultStatus.Disabled(VaultStatus.DisabledReason.SameVault))

            val otherState = state.vaultList.find { it.vaultWithItemCount.vault.shareId == OTHER_SHARE_ID }
            assertThat(otherState).isNotNull()
            assertThat(otherState!!.status).isEqualTo(VaultStatus.Enabled)

            assertThat(state.disabledFolderId).isEqualTo(None)
            assertThat(state.disabledFolderItemCount).isEqualTo(0)
        }
    }

    @Test
    fun `two root-level items in same vault disables source vault`() = runTest {
        val repo = FakeBulkMoveToVaultRepository().apply {
            runBlocking { save(mapOf(SHARE_ID to listOf(ITEM_ID, ITEM_ID_2)).toBulkMoveToVaultSelection()) }
        }
        val (currentVault, otherVault) = initialVaults()
        observeVaults.sendResult(Result.success(listOf(currentVault, otherVault)))

        buildInstance(bulkRepo = repo).state.test {
            val state = awaitItem()
            val currentState = state.vaultList.find { it.vaultWithItemCount.vault.shareId == SHARE_ID }
            assertThat(currentState!!.status).isEqualTo(VaultStatus.Disabled(VaultStatus.DisabledReason.SameVault))
            assertThat(state.disabledFolderId).isEqualTo(None)
            assertThat(state.disabledFolderItemCount).isEqualTo(0)
        }
    }

    @Test
    fun `mixed root and folder items keep source vault enabled`() = runTest {
        val repo = FakeBulkMoveToVaultRepository().apply {
            runBlocking {
                save(
                    mapOf(
                        SHARE_ID to mapOf(
                            ParentContainer.Share to setOf(ITEM_ID, ITEM_ID_2),
                            ParentContainer.Folder(FOLDER_ID) to setOf(ITEM_ID_IN_FOLDER)
                        )
                    )
                )
            }
        }
        val (currentVault, otherVault) = initialVaults()
        observeVaults.sendResult(Result.success(listOf(currentVault, otherVault)))

        buildInstance(bulkRepo = repo).state.test {
            val state = awaitItem()
            val currentState = state.vaultList.find { it.vaultWithItemCount.vault.shareId == SHARE_ID }
            assertThat(currentState!!.status).isEqualTo(VaultStatus.Enabled)
            assertThat(state.disabledFolderId).isEqualTo(None)
            assertThat(state.disabledFolderItemCount).isEqualTo(0)
        }
    }

    @Test
    fun `single folder items disable that folder with item count`() = runTest {
        val repo = FakeBulkMoveToVaultRepository().apply {
            runBlocking {
                save(
                    mapOf(
                        SHARE_ID to mapOf(
                            ParentContainer.Folder(FOLDER_ID) to setOf(ITEM_ID, ITEM_ID_2)
                        )
                    )
                )
            }
        }
        val (currentVault, otherVault) = initialVaults()
        observeVaults.sendResult(Result.success(listOf(currentVault, otherVault)))

        buildInstance(bulkRepo = repo).state.test {
            val state = awaitItem()
            val currentState = state.vaultList.find { it.vaultWithItemCount.vault.shareId == SHARE_ID }
            assertThat(currentState!!.status).isEqualTo(VaultStatus.Enabled)
            assertThat(state.disabledFolderId).isEqualTo(FOLDER_ID.toOption())
            assertThat(state.disabledFolderItemCount).isEqualTo(2)
        }
    }

    @Test
    fun `items in different folders do not disable any folder`() = runTest {
        val repo = FakeBulkMoveToVaultRepository().apply {
            runBlocking {
                save(
                    mapOf(
                        SHARE_ID to mapOf(
                            ParentContainer.Folder(FOLDER_ID) to setOf(ITEM_ID),
                            ParentContainer.Folder(FOLDER_ID_2) to setOf(ITEM_ID_2)
                        )
                    )
                )
            }
        }
        val (currentVault, otherVault) = initialVaults()
        observeVaults.sendResult(Result.success(listOf(currentVault, otherVault)))

        buildInstance(bulkRepo = repo).state.test {
            val state = awaitItem()
            assertThat(state.disabledFolderId).isEqualTo(None)
            assertThat(state.disabledFolderItemCount).isEqualTo(0)
            val currentState = state.vaultList.find { it.vaultWithItemCount.vault.shareId == SHARE_ID }
            assertThat(currentState!!.status).isEqualTo(VaultStatus.Enabled)
        }
    }

    @Test
    fun `does not restart folder observers when vault order changes`() = runTest {
        val (currentVault, otherVault) = initialVaults()
        val instance = buildInstance()

        instance.state.test {
            observeVaults.sendResult(Result.success(listOf(currentVault, otherVault)))
            awaitItem()

            observeVaults.sendResult(Result.success(listOf(otherVault, currentVault)))
            awaitItem()

            assertThat(
                observeFolders.invocationCount(
                    userId = currentVault.vault.userId,
                    shareId = currentVault.vault.shareId
                )
            ).isEqualTo(1)
            assertThat(
                observeFolders.invocationCount(
                    userId = otherVault.vault.userId,
                    shareId = otherVault.vault.shareId
                )
            ).isEqualTo(1)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observes duplicated share key once`() = runTest {
        val (currentVault, otherVault) = initialVaults()
        val instance = buildInstance()

        instance.state.test {
            observeVaults.sendResult(Result.success(listOf(currentVault, currentVault, otherVault)))
            awaitItem()

            assertThat(
                observeFolders.invocationCount(
                    userId = currentVault.vault.userId,
                    shareId = currentVault.vault.shareId
                )
            ).isEqualTo(1)
            assertThat(
                observeFolders.invocationCount(
                    userId = otherVault.vault.userId,
                    shareId = otherVault.vault.shareId
                )
            ).isEqualTo(1)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `filters to only shared vaults when filter mode is Shared`() = runTest {
        val sharedVault = VaultWithItemCount(
            vault = VaultTestFactory.create(shareId = ShareId("shared-vault"), name = "shared", shared = true),
            activeItemCount = 1,
            trashedItemCount = 0
        )
        val nonSharedVault = VaultWithItemCount(
            vault = VaultTestFactory.create(shareId = ShareId("non-shared-vault"), name = "non-shared"),
            activeItemCount = 1,
            trashedItemCount = 0
        )
        observeVaults.sendResult(Result.success(listOf(sharedVault, nonSharedVault)))

        buildInstance(filter = MigrateVaultFilter.Shared).state.test {
            val state = awaitItem()
            assertThat(state.vaultList).hasSize(1)
            assertThat(state.vaultList.first().vaultWithItemCount.vault.shareId).isEqualTo(ShareId("shared-vault"))
        }
    }

    @Test
    fun `vault selection updates selectedShareId in state`() = runTest {
        val (currentVault, otherVault) = initialVaults()
        observeVaults.sendResult(Result.success(listOf(currentVault, otherVault)))

        val instance = buildInstance()
        instance.onVaultSelected(otherVault.vault.shareId)
        instance.state.test {
            val state = awaitItem()
            assertThat(state.selectedShareId).isEqualTo(otherVault.vault.shareId.toOption())
        }
    }

    @Test
    fun `folder selection updates selectedShareId in state`() = runTest {
        val repo = FakeBulkMoveToVaultRepository().apply {
            runBlocking {
                save(
                    mapOf(
                        SHARE_ID to mapOf(
                            ParentContainer.Folder(FOLDER_ID) to setOf(ITEM_ID)
                        )
                    )
                )
            }
        }
        val (currentVault, otherVault) = initialVaults()
        observeVaults.sendResult(Result.success(listOf(currentVault, otherVault)))

        val instance = buildInstance(bulkRepo = repo)
        instance.onFolderSelected(SHARE_ID, FOLDER_ID)
        instance.state.test {
            val state = awaitItem()
            assertThat(state.selectedShareId).isEqualTo(SHARE_ID.toOption())
        }
    }

    private fun initialVaults(): Pair<VaultWithItemCount, VaultWithItemCount> = Pair(
        VaultWithItemCount(
            vault = VaultTestFactory.create(shareId = SHARE_ID, name = "vault1"),
            activeItemCount = 1,
            trashedItemCount = 0
        ),
        VaultWithItemCount(
            vault = VaultTestFactory.create(shareId = OTHER_SHARE_ID, name = "vault2"),
            activeItemCount = 1,
            trashedItemCount = 0
        )
    )

    companion object {
        private val SHARE_ID = ShareId("123")
        private val OTHER_SHARE_ID = ShareId("OTHER_SHARE_ID")
        private val ITEM_ID = ItemId("456")
        private val ITEM_ID_2 = ItemId("457")
        private val ITEM_ID_IN_FOLDER = ItemId("458")
        private val FOLDER_ID = FolderId("folder-789")
        private val FOLDER_ID_2 = FolderId("folder-790")
    }
}
