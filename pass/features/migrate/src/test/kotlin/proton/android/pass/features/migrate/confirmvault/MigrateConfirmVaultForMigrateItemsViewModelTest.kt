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
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.common.api.Some
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.repositories.MigrateItemsResult
import proton.android.pass.data.fakes.repositories.FakeBulkMoveToVaultRepository
import proton.android.pass.data.api.repositories.flattenByShare
import proton.android.pass.data.api.repositories.toBulkMoveToVaultSelection
import proton.android.pass.data.fakes.usecases.FakeMigrateItems
import proton.android.pass.data.fakes.usecases.FakeMigrateVault
import proton.android.pass.data.fakes.usecases.FakeObserveVaultsWithItemCount
import proton.android.pass.data.fakes.usecases.folders.FakeDissolveFolder
import proton.android.pass.data.fakes.usecases.folders.FakeMoveAllItemsInFolder
import proton.android.pass.data.fakes.usecases.folders.FakeMoveFolder
import proton.android.pass.data.fakes.usecases.folders.FakeMoveItemsInsideShare
import proton.android.pass.data.fakes.usecases.items.FakeGetMigrationItemsSelection
import proton.android.pass.data.fakes.usecases.folders.FakeObserveFoldersByParentId
import proton.android.pass.data.fakes.usecases.securelink.FakeObserveHasAssociatedSecureLinks
import proton.android.pass.data.fakes.usecases.shares.FakeObserveShare
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.features.migrate.MigrateModeArg
import proton.android.pass.features.migrate.MigrateModeValue
import proton.android.pass.features.migrate.MigrateSnackbarMessage
import proton.android.pass.features.migrate.MigrateVaultFilter
import proton.android.pass.features.migrate.MigrateVaultFilterArg
import proton.android.pass.notifications.fakes.FakeSnackbarDispatcher
import proton.android.pass.preferences.FakeInternalSettingsRepository
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.SavedStateHandleTestFactory
import proton.android.pass.test.domain.ItemTestFactory
import proton.android.pass.test.domain.VaultTestFactory

class MigrateConfirmVaultForMigrateItemsViewModelTest {

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var instance: MigrateConfirmVaultViewModel
    private lateinit var migrateItem: FakeMigrateItems
    private lateinit var migrateVault: FakeMigrateVault
    private lateinit var observeVaults: FakeObserveVaultsWithItemCount
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
        snackbarDispatcher = FakeSnackbarDispatcher()
        bulkMoveToVaultRepository = FakeBulkMoveToVaultRepository().apply {
            runBlocking { save(mapOf(SHARE_ID to listOf(ITEM_ID)).toBulkMoveToVaultSelection()) }
        }
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
                set(MigrateModeArg.key, MODE.name)
                set(MigrateVaultFilterArg.key, MigrateVaultFilter.All.name)
            },
            observeShare = observeShare,
            settingsRepository = settingsRepository,
            observeFolders = FakeObserveFoldersByParentId(),
            getMigrationItemsSelection = FakeGetMigrationItemsSelection()
        )
    }

    @Test
    fun `emits initial state`() = runTest {
        instance.state.test {
            val expected = MigrateConfirmVaultUiState.initial(
                mode = MigrateMode.MigrateSelectedItems(1)
            ).copy(
                isLoading = IsLoadingState.Loading // vault list is loading
            )
            assertThat(awaitItem()).isEqualTo(expected)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `can migrate item after selecting destination vault`() = runTest {
        val (sourceVault, destVault) = initialVaults()
        val expectedItem = ItemTestFactory.create().copy(id = ITEM_ID, shareId = DESTINATION_SHARE_ID)
        migrateItem.setResult(Result.success(MigrateItemsResult.AllMigrated(listOf(expectedItem))))

        instance.state.test {
            observeVaults.sendResult(Result.success(listOf(sourceVault, destVault)))
            // Wait until we have a non-loading state with vault list populated
            var state = awaitItem()
            while (state.vaultList.isEmpty()) {
                state = awaitItem()
            }

            instance.onVaultSelected(destVault.vault.shareId)
            state = awaitItem() // selectedShareId updated
            instance.onConfirm()

            // Wait for migration event
            state = awaitItem()
            while (state.event.isEmpty()) {
                state = awaitItem()
            }

            assertThat(state.event.isEmpty()).isFalse()
            val eventCasted = state.event as Some<ConfirmMigrateEvent>
            assertThat(eventCasted.value).isInstanceOf(ConfirmMigrateEvent.ItemMigrated::class.java)

            val migratedEvent = eventCasted.value as ConfirmMigrateEvent.ItemMigrated
            assertThat(migratedEvent.itemId).isEqualTo(expectedItem.id)
            assertThat(migratedEvent.shareId).isEqualTo(expectedItem.shareId)

            val snackbarMessage = snackbarDispatcher.snackbarMessage.first()
            assertThat(snackbarMessage.isNotEmpty()).isTrue()

            val message = snackbarMessage.value()!!
            assertThat(message).isInstanceOf(MigrateSnackbarMessage.ItemMigrated::class.java)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `displays error if cannot migrate items`() = runTest {
        val (sourceVault, destVault) = initialVaults()
        migrateItem.setResult(Result.failure(IllegalStateException("test")))

        instance.state.test {
            observeVaults.sendResult(Result.success(listOf(sourceVault, destVault)))
            // Wait until vault list is populated
            var state = awaitItem()
            while (state.vaultList.isEmpty()) {
                state = awaitItem()
            }

            instance.onVaultSelected(destVault.vault.shareId)
            awaitItem() // selectedShareId updated
            instance.onConfirm()

            cancelAndIgnoreRemainingEvents()
        }

        // After migration fails, verify the error snackbar was dispatched
        val snackbarMessage = snackbarDispatcher.snackbarMessage.first()
        assertThat(snackbarMessage.isNotEmpty()).isTrue()

        val message = snackbarMessage.value()!!
        assertThat(message).isInstanceOf(MigrateSnackbarMessage.ItemNotMigrated::class.java)
    }

    @Test
    fun `bulk repository retains items before migration`() = runTest {
        val bulkMemory = bulkMoveToVaultRepository.observe().first()
        assertThat(bulkMemory.value()?.flattenByShare()).isEqualTo(mapOf(SHARE_ID to listOf(ITEM_ID)))
    }

    private fun initialVaults(): Pair<VaultWithItemCount, VaultWithItemCount> = Pair(
        VaultWithItemCount(
            vault = VaultTestFactory.create(shareId = SHARE_ID, name = "vault1"),
            activeItemCount = 1,
            trashedItemCount = 0
        ),
        VaultWithItemCount(
            vault = VaultTestFactory.create(shareId = DESTINATION_SHARE_ID, name = "vault2"),
            activeItemCount = 1,
            trashedItemCount = 0
        )
    )

    companion object {
        private val SHARE_ID = ShareId("123")
        private val DESTINATION_SHARE_ID = ShareId("456")
        private val ITEM_ID = ItemId("789")

        private val MODE = MigrateModeValue.SelectedItems
    }
}
