/*
 * Copyright (c) 2023-2026 Proton AG
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

import app.cash.turbine.test
import app.cash.turbine.ReceiveTurbine
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.commonui.fakes.FakeSavedStateHandleProvider
import proton.android.pass.data.api.repositories.ParentContainer
import proton.android.pass.data.fakes.repositories.FakeBulkMoveToVaultRepository
import proton.android.pass.data.api.repositories.toBulkMoveToVaultSelection
import proton.android.pass.data.fakes.usecases.FakeObserveVaultsWithItemCount
import proton.android.pass.data.fakes.usecases.folders.FakeObserveFolders
import proton.android.pass.common.api.None
import proton.android.pass.common.api.toOption
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.features.migrate.MigrateModeArg
import proton.android.pass.features.migrate.MigrateModeValue
import proton.android.pass.features.migrate.MigrateVaultFilter
import proton.android.pass.features.migrate.MigrateVaultFilterArg
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.notifications.fakes.FakeSnackbarDispatcher
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.VaultTestFactory

class MigrateSelectVaultViewModelTest {

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var instance: MigrateSelectVaultViewModel
    private lateinit var observeVaults: FakeObserveVaultsWithItemCount
    private lateinit var observeFolders: FakeObserveFolders
    private lateinit var snackbarDispatcher: FakeSnackbarDispatcher
    private lateinit var bulkMoveToVaultRepository: FakeBulkMoveToVaultRepository

    @Before
    fun setup() {
        observeVaults = FakeObserveVaultsWithItemCount()
        observeFolders = FakeObserveFolders()
        snackbarDispatcher = FakeSnackbarDispatcher()
        bulkMoveToVaultRepository = FakeBulkMoveToVaultRepository().apply {
            runBlocking { save(mapOf(SHARE_ID to listOf(ITEM_ID)).toBulkMoveToVaultSelection()) }

        }
        instance = MigrateSelectVaultViewModel(
            observeVaults = observeVaults,
            observeFolders = observeFolders,
            snackbarDispatcher = snackbarDispatcher,
            bulkMoveToVaultRepository = bulkMoveToVaultRepository,
            savedStateHandle = FakeSavedStateHandleProvider().apply {
                get()[MigrateVaultFilterArg.key] = MigrateVaultFilter.All.name
                get()[MigrateModeArg.key] = MigrateModeValue.SelectedItems.name
                get()[CommonOptionalNavArgId.ItemId.key] = ITEM_ID.id
            }
        )
    }

    @Test
    fun `marks the current vault as not enabled`() = runTest {
        val (currentVault, otherVault) = initialVaults()
        observeVaults.sendResult(Result.success(listOf(currentVault, otherVault)))

        val expected = listOf(
            MigrateVaultState(
                vaultWithItemCount = currentVault,
                status = VaultStatus.Disabled(VaultStatus.DisabledReason.SameVault)
            ),
            MigrateVaultState(otherVault, VaultStatus.Enabled)
        )
        instance.state.test {
            val item = awaitItem()
            require(item is MigrateSelectVaultUiState.Success)
            assertThat(item.vaultList).isEqualTo(expected)
            assertThat(item.event.value()).isNull()
            assertThat(item.disabledFolderId).isEqualTo(None)
            assertThat(item.disabledFolderItemCount).isEqualTo(0)
        }
    }

    @Test
    fun `two items at root level in same vault disables source vault`() = runTest {
        val repoWithTwoItems = FakeBulkMoveToVaultRepository().apply {
            runBlocking { save(mapOf(SHARE_ID to listOf(ITEM_ID, ITEM_ID_2)).toBulkMoveToVaultSelection()) }
        }
        val vm = MigrateSelectVaultViewModel(
            observeVaults = observeVaults,
            observeFolders = observeFolders,
            snackbarDispatcher = snackbarDispatcher,
            bulkMoveToVaultRepository = repoWithTwoItems,
            savedStateHandle = FakeSavedStateHandleProvider().apply {
                get()[MigrateVaultFilterArg.key] = MigrateVaultFilter.All.name
                get()[MigrateModeArg.key] = MigrateModeValue.SelectedItems.name
            }
        )
        val (currentVault, otherVault) = initialVaults()
        observeVaults.sendResult(Result.success(listOf(currentVault, otherVault)))

        val expected = listOf(
            MigrateVaultState(
                vaultWithItemCount = currentVault,
                status = VaultStatus.Disabled(VaultStatus.DisabledReason.SameVault)
            ),
            MigrateVaultState(otherVault, VaultStatus.Enabled)
        )
        vm.state.test {
            val item = awaitItem()
            require(item is MigrateSelectVaultUiState.Success)
            assertThat(item.vaultList).isEqualTo(expected)
            assertThat(item.disabledFolderId).isEqualTo(None)
            assertThat(item.disabledFolderItemCount).isEqualTo(0)
        }
    }

    @Test
    fun `mixed items at root and in folder do not disable source vault`() = runTest {
        val repoWithMixedItems = FakeBulkMoveToVaultRepository().apply {
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
        val vm = MigrateSelectVaultViewModel(
            observeVaults = observeVaults,
            observeFolders = observeFolders,
            snackbarDispatcher = snackbarDispatcher,
            bulkMoveToVaultRepository = repoWithMixedItems,
            savedStateHandle = FakeSavedStateHandleProvider().apply {
                get()[MigrateVaultFilterArg.key] = MigrateVaultFilter.All.name
                get()[MigrateModeArg.key] = MigrateModeValue.SelectedItems.name
            }
        )
        val (currentVault, otherVault) = initialVaults()
        observeVaults.sendResult(Result.success(listOf(currentVault, otherVault)))

        val expected = listOf(
            MigrateVaultState(vaultWithItemCount = currentVault, status = VaultStatus.Enabled),
            MigrateVaultState(otherVault, VaultStatus.Enabled)
        )
        vm.state.test {
            val item = awaitItem()
            require(item is MigrateSelectVaultUiState.Success)
            assertThat(item.vaultList).isEqualTo(expected)
            assertThat(item.disabledFolderId).isEqualTo(None)
            assertThat(item.disabledFolderItemCount).isEqualTo(0)
        }
    }

    @Test
    fun `items in different folders do not disable any folder`() = runTest {
        val repoWithDifferentFolders = FakeBulkMoveToVaultRepository().apply {
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
        val vm = MigrateSelectVaultViewModel(
            observeVaults = observeVaults,
            observeFolders = observeFolders,
            snackbarDispatcher = snackbarDispatcher,
            bulkMoveToVaultRepository = repoWithDifferentFolders,
            savedStateHandle = FakeSavedStateHandleProvider().apply {
                get()[MigrateVaultFilterArg.key] = MigrateVaultFilter.All.name
                get()[MigrateModeArg.key] = MigrateModeValue.SelectedItems.name
            }
        )
        val (currentVault, otherVault) = initialVaults()
        observeVaults.sendResult(Result.success(listOf(currentVault, otherVault)))

        vm.state.test {
            val item = awaitItem()
            require(item is MigrateSelectVaultUiState.Success)
            assertThat(item.disabledFolderId).isEqualTo(None)
            assertThat(item.disabledFolderItemCount).isEqualTo(0)
            assertThat(item.vaultList.first().status).isEqualTo(VaultStatus.Enabled)
        }
    }

    @Test
    fun `does not restart folder observers when vault order changes`() = runTest {
        val (currentVault, otherVault) = initialVaults()

        instance.state.test {
            observeVaults.sendResult(Result.success(listOf(currentVault, otherVault)))
            awaitSuccess()

            observeVaults.sendResult(Result.success(listOf(otherVault, currentVault)))
            awaitSuccess()

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
        }
    }

    @Test
    fun `observes duplicated share key once`() = runTest {
        val (currentVault, otherVault) = initialVaults()

        instance.state.test {
            observeVaults.sendResult(Result.success(listOf(currentVault, currentVault, otherVault)))
            awaitSuccess()

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
        }
    }

    private suspend fun ReceiveTurbine<MigrateSelectVaultUiState>.awaitSuccess(): MigrateSelectVaultUiState.Success {
        while (true) {
            val state = awaitItem()
            if (state is MigrateSelectVaultUiState.Success) return state
        }
    }

    @Test
    fun `item in folder enables source vault`() = runTest {
        val vm = buildInstanceWithFolderSelection(FOLDER_ID)
        val (currentVault, otherVault) = initialVaults()
        observeVaults.sendResult(Result.success(listOf(currentVault, otherVault)))

        val expected = listOf(
            MigrateVaultState(vaultWithItemCount = currentVault, status = VaultStatus.Enabled),
            MigrateVaultState(otherVault, VaultStatus.Enabled)
        )
        vm.state.test {
            val item = awaitItem()
            require(item is MigrateSelectVaultUiState.Success)
            assertThat(item.vaultList).isEqualTo(expected)
            assertThat(item.disabledFolderId).isEqualTo(FOLDER_ID.toOption())
            assertThat(item.disabledFolderItemCount).isEqualTo(1)
        }
    }

    @Test
    fun `selecting same folder emits migrate event`() = runTest {
        val vm = buildInstanceWithFolderSelection(FOLDER_ID)
        val (currentVault, otherVault) = initialVaults()
        observeVaults.sendResult(Result.success(listOf(currentVault, otherVault)))

        vm.state.test {
            awaitSuccess()
            vm.onFolderSelected(SHARE_ID, FOLDER_ID)
            val state = awaitSuccess()
            assertThat(state.event.value()).isEqualTo(
                SelectVaultEvent.VaultSelectedForMigrateItem(
                    destinationShareId = SHARE_ID,
                    destFolderId = FOLDER_ID.toOption()
                )
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun buildInstanceWithFolderSelection(folderId: FolderId): MigrateSelectVaultViewModel {
        val repo = FakeBulkMoveToVaultRepository().apply {
            runBlocking {
                save(
                    mapOf(
                        SHARE_ID to mapOf(
                            ParentContainer.Folder(folderId) to setOf(ITEM_ID)
                        )
                    )
                )
            }
        }
        return MigrateSelectVaultViewModel(
            observeVaults = observeVaults,
            observeFolders = observeFolders,
            snackbarDispatcher = snackbarDispatcher,
            bulkMoveToVaultRepository = repo,
            savedStateHandle = FakeSavedStateHandleProvider().apply {
                get()[MigrateVaultFilterArg.key] = MigrateVaultFilter.All.name
                get()[MigrateModeArg.key] = MigrateModeValue.SelectedItems.name
            }
        )
    }

    private fun initialVaults(): Pair<VaultWithItemCount, VaultWithItemCount> = Pair(
        VaultWithItemCount(
            vault = VaultTestFactory.create(shareId = SHARE_ID, name = "vault1"),
            activeItemCount = 1,
            trashedItemCount = 0
        ),
        VaultWithItemCount(
            vault = VaultTestFactory.create(shareId = ShareId("OTHER_SHARE_ID"), name = "vault2"),
            activeItemCount = 1,
            trashedItemCount = 0
        )
    )

    companion object {
        private val SHARE_ID = ShareId("123")
        private val ITEM_ID = ItemId("456")
        private val ITEM_ID_2 = ItemId("457")
        private val ITEM_ID_IN_FOLDER = ItemId("458")
        private val FOLDER_ID = FolderId("folder-789")
        private val FOLDER_ID_2 = FolderId("folder-790")
    }

}
