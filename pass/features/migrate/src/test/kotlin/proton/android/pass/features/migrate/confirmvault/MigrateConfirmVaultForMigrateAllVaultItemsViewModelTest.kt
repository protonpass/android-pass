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

package proton.android.pass.features.migrate.confirmvault

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.common.api.Some
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.repositories.BulkMoveToVaultEvent
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
import proton.android.pass.features.migrate.MigrateSnackbarMessage
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.fakes.FakeSnackbarDispatcher
import proton.android.pass.preferences.FakeInternalSettingsRepository
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.SavedStateHandleTestFactory
import proton.android.pass.domain.items.MigrationItemsSelection
import proton.android.pass.test.domain.FolderTestFactory
import proton.android.pass.test.domain.ItemTestFactory
import proton.android.pass.test.domain.VaultTestFactory

internal class MigrateConfirmVaultForMigrateAllVaultItemsViewModelTest {

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var instance: MigrateConfirmVaultViewModel
    private lateinit var migrateItem: FakeMigrateItems
    private lateinit var migrateVault: FakeMigrateVault
    private lateinit var observeVaults: FakeObserveVaultsWithItemCount
    private lateinit var observeFolders: FakeObserveFoldersByParentId
    private lateinit var snackbarDispatcher: FakeSnackbarDispatcher
    private lateinit var bulkMoveToVaultRepository: FakeBulkMoveToVaultRepository
    private lateinit var observeHasAssociatedSecureLinks: FakeObserveHasAssociatedSecureLinks
    private lateinit var observeShare: FakeObserveShare
    private lateinit var settingsRepository: FakeInternalSettingsRepository

    @Before
    fun setup() {
        migrateItem = FakeMigrateItems()
        migrateVault = FakeMigrateVault()
        observeVaults = FakeObserveVaultsWithItemCount()
        observeFolders = FakeObserveFoldersByParentId()
        snackbarDispatcher = FakeSnackbarDispatcher()
        bulkMoveToVaultRepository = FakeBulkMoveToVaultRepository()
        observeHasAssociatedSecureLinks = FakeObserveHasAssociatedSecureLinks()
        observeShare = FakeObserveShare()
        settingsRepository = FakeInternalSettingsRepository()

        instance = MigrateConfirmVaultViewModel(
            migrator = MigrateConfirmVaultMigrator(
                migrateItems = migrateItem,
                migrateVault = migrateVault,
                moveFolder = FakeMoveFolder(),
                dissolveFolder = FakeDissolveFolder(),
                moveAllItemsInFolder = FakeMoveAllItemsInFolder(),
                moveItemsInsideShare = FakeMoveItemsInsideShare(),
                snackbarDispatcher = snackbarDispatcher,
                bulkMoveToVaultRepository = bulkMoveToVaultRepository
            ),
            snackbarDispatcher = snackbarDispatcher,
            observeVaults = observeVaults,
            bulkMoveToVaultRepository = bulkMoveToVaultRepository,
            observeHasAssociatedSecureLinks = observeHasAssociatedSecureLinks,
            savedStateHandle = SavedStateHandleTestFactory.create().apply {
                set(CommonNavArgId.ShareId.key, SHARE_ID.id)
                set(MigrateModeArg.key, MODE.name)
            },
            observeShare = observeShare,
            settingsRepository = settingsRepository,
            observeFolders = observeFolders,
            getMigrationItemsSelection = FakeGetMigrationItemsSelection()
        )
    }

    @Test
    fun `emits initial state`() = runTest {
        instance.state.test {
            val expected = MigrateConfirmVaultUiState.initial(MigrateMode.MigrateAll).copy(
                isLoading = IsLoadingState.Loading // vault list is loading
            )
            assertThat(awaitItem()).isEqualTo(expected)
        }
    }

    @Test
    fun `hasItemsWithHighRevisionCount is true when vault has items exceeding revision limit`() = runTest {
        val highRevisionItem = ItemTestFactory.create()
            .copy(revision = MigrationItemsSelection.REVISION_LIMIT + 1)
        val fakeGetMigration = FakeGetMigrationItemsSelection().apply {
            setMigrationItemsSelection(MigrationItemsSelection(listOf(highRevisionItem)))
        }
        val testInstance = MigrateConfirmVaultViewModel(
            migrator = MigrateConfirmVaultMigrator(
                migrateItems = migrateItem,
                migrateVault = migrateVault,
                moveFolder = FakeMoveFolder(),
                dissolveFolder = FakeDissolveFolder(),
                moveAllItemsInFolder = FakeMoveAllItemsInFolder(),
                moveItemsInsideShare = FakeMoveItemsInsideShare(),
                snackbarDispatcher = snackbarDispatcher,
                bulkMoveToVaultRepository = bulkMoveToVaultRepository
            ),
            snackbarDispatcher = snackbarDispatcher,
            observeVaults = observeVaults,
            bulkMoveToVaultRepository = bulkMoveToVaultRepository,
            observeHasAssociatedSecureLinks = observeHasAssociatedSecureLinks,
            savedStateHandle = SavedStateHandleTestFactory.create().apply {
                set(CommonNavArgId.ShareId.key, SHARE_ID.id)
                set(MigrateModeArg.key, MODE.name)
            },
            observeShare = observeShare,
            settingsRepository = settingsRepository,
            observeFolders = observeFolders,
            getMigrationItemsSelection = fakeGetMigration
        )

        testInstance.state.test {
            assertThat(awaitItem().hasItemsWithHighRevisionCount).isTrue()
        }
    }

    @Test
    fun `can migrate items after selecting destination vault`() = runTest {
        val (sourceVault, destVault) = initialVaults()
        observeVaults.sendResult(Result.success(listOf(sourceVault, destVault)))

        instance.onVaultSelected(destVault.vault.shareId)
        instance.onConfirm()
        instance.state.test {
            val state = awaitItem()
            val eventCasted = state.event as Some<ConfirmMigrateEvent>
            assertThat(eventCasted.value).isInstanceOf(ConfirmMigrateEvent.AllItemsMigrated::class.java)
        }

        val snackbarMessage = snackbarDispatcher.snackbarMessage.first()
        assertThat(snackbarMessage.isNotEmpty()).isTrue()

        val message = snackbarMessage.value()!!
        assertThat(message).isInstanceOf(MigrateSnackbarMessage.VaultItemsMigrated::class.java)

        val expected = FakeMigrateVault.Memory(SHARE_ID, DESTINATION_SHARE_ID)
        assertThat(migrateVault.memory()).isEqualTo(listOf(expected))
    }

    @Test
    fun `displays error if cannot migrate items`() = runTest {
        val (sourceVault, destVault) = initialVaults()
        observeVaults.sendResult(Result.success(listOf(sourceVault, destVault)))
        migrateVault.setResult(Result.failure(IllegalStateException("test")))

        instance.onVaultSelected(destVault.vault.shareId)
        instance.onConfirm()
        instance.state.test {
            val state = awaitItem()
            assertThat(state.isLoading).isInstanceOf(IsLoadingState.NotLoading::class.java)
        }

        val snackbarMessage = snackbarDispatcher.snackbarMessage.first()
        assertThat(snackbarMessage.isNotEmpty()).isTrue()

        val message = snackbarMessage.value()!!
        assertThat(message).isInstanceOf(MigrateSnackbarMessage.VaultItemsNotMigrated::class.java)

        val expected = FakeMigrateVault.Memory(SHARE_ID, DESTINATION_SHARE_ID)
        assertThat(migrateVault.memory()).isEqualTo(listOf(expected))

        // No event should have been emitted
        val bulkEvent = bulkMoveToVaultRepository.observeEvent().first()
        assertThat(bulkEvent).isEqualTo(BulkMoveToVaultEvent.Idle)
    }

    @Test
    fun `marks source vault as disabled in vault list`() = runTest {
        val (sourceVault, destVault) = initialVaults()
        observeVaults.sendResult(Result.success(listOf(sourceVault, destVault)))

        instance.state.test {
            val state = awaitItem()
            val sourceState = state.vaultList.find { it.vaultWithItemCount.vault.shareId == SHARE_ID }
            assertThat(sourceState).isNotNull()
            assertThat(sourceState!!.status).isEqualTo(VaultStatus.Disabled(VaultStatus.DisabledReason.SameVault))

            val destState = state.vaultList.find { it.vaultWithItemCount.vault.shareId == DESTINATION_SHARE_ID }
            assertThat(destState).isNotNull()
            assertThat(destState!!.status).isEqualTo(VaultStatus.Enabled)
        }
    }

    @Test
    fun `source vault exposes its folders but keeps root disabled when folders are empty`() = runTest {
        observeFolders.sendResult(
            Result.success(listOf(FolderTestFactory.create(shareId = SHARE_ID, folderId = FOLDER_ID)))
        )
        val (sourceVault, destVault) = initialVaults()
        observeVaults.sendResult(Result.success(listOf(sourceVault, destVault)))

        instance.state.test {
            val state = awaitItem()
            val sourceState = state.vaultList.find { it.vaultWithItemCount.vault.shareId == SHARE_ID }
            assertThat(sourceState).isNotNull()
            assertThat(sourceState!!.status).isEqualTo(VaultStatus.Disabled(VaultStatus.DisabledReason.SameVault))
            assertThat(sourceState.folderTree).isNotEmpty()
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `source vault with items in folders is enabled as destination`() = runTest {
        val fakeGetMigration = FakeGetMigrationItemsSelection().apply {
            setMigrationItemsSelection(
                MigrationItemsSelection(
                    items = listOf(ItemTestFactory.create(shareId = SHARE_ID, folderId = FOLDER_ID))
                )
            )
        }
        val testInstance = MigrateConfirmVaultViewModel(
            migrator = MigrateConfirmVaultMigrator(
                migrateItems = migrateItem,
                migrateVault = migrateVault,
                moveFolder = FakeMoveFolder(),
                dissolveFolder = FakeDissolveFolder(),
                moveAllItemsInFolder = FakeMoveAllItemsInFolder(),
                moveItemsInsideShare = FakeMoveItemsInsideShare(),
                snackbarDispatcher = snackbarDispatcher,
                bulkMoveToVaultRepository = bulkMoveToVaultRepository
            ),
            snackbarDispatcher = snackbarDispatcher,
            observeVaults = observeVaults,
            bulkMoveToVaultRepository = bulkMoveToVaultRepository,
            observeHasAssociatedSecureLinks = observeHasAssociatedSecureLinks,
            savedStateHandle = SavedStateHandleTestFactory.create().apply {
                set(CommonNavArgId.ShareId.key, SHARE_ID.id)
                set(MigrateModeArg.key, MODE.name)
            },
            observeShare = observeShare,
            settingsRepository = settingsRepository,
            observeFolders = observeFolders,
            getMigrationItemsSelection = fakeGetMigration
        )

        observeFolders.sendResult(
            Result.success(listOf(FolderTestFactory.create(shareId = SHARE_ID, folderId = FOLDER_ID)))
        )
        val (sourceVault, destVault) = initialVaults()
        observeVaults.sendResult(Result.success(listOf(sourceVault, destVault)))

        testInstance.state.test {
            val state = awaitItem()
            val sourceState = state.vaultList.find { it.vaultWithItemCount.vault.shareId == SHARE_ID }
            assertThat(sourceState).isNotNull()
            assertThat(sourceState!!.status).isEqualTo(VaultStatus.Enabled)
            assertThat(sourceState.folderTree).isNotEmpty()
            cancelAndConsumeRemainingEvents()
        }
    }

    private fun initialVaults(): Pair<VaultWithItemCount, VaultWithItemCount> = Pair(
        VaultWithItemCount(
            vault = VaultTestFactory.create(shareId = SHARE_ID),
            activeItemCount = 1,
            trashedItemCount = 0
        ),
        VaultWithItemCount(
            vault = VaultTestFactory.create(shareId = DESTINATION_SHARE_ID),
            activeItemCount = 1,
            trashedItemCount = 0
        )
    )

    companion object {
        private val SHARE_ID = ShareId("123")
        private val DESTINATION_SHARE_ID = ShareId("456")
        private val FOLDER_ID = FolderId("folder-1")

        private val MODE = MigrateModeValue.AllVaultItems
    }
}
