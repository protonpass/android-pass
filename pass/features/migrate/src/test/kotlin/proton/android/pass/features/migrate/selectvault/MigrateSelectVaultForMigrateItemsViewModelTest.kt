/*
 * Copyright (c) 2023-2024 Proton AG
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
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.commonui.fakes.TestSavedStateHandleProvider
import proton.android.pass.data.fakes.repositories.TestBulkMoveToVaultRepository
import proton.android.pass.data.fakes.usecases.TestCanPerformPaidAction
import proton.android.pass.data.fakes.usecases.TestObserveVaultsWithItemCount
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.features.migrate.MigrateModeArg
import proton.android.pass.features.migrate.MigrateModeValue
import proton.android.pass.features.migrate.MigrateVaultFilter
import proton.android.pass.features.migrate.MigrateVaultFilterArg
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.notifications.fakes.TestSnackbarDispatcher
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.TestVault

class MigrateSelectVaultForMigrateItemsViewModelTest {

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var instance: MigrateSelectVaultViewModel
    private lateinit var observeVaults: TestObserveVaultsWithItemCount
    private lateinit var canPerformPaidAction: TestCanPerformPaidAction
    private lateinit var snackbarDispatcher: TestSnackbarDispatcher
    private lateinit var savedState: TestSavedStateHandleProvider
    private lateinit var bulkMoveToVaultRepository: TestBulkMoveToVaultRepository

    @Before
    fun setup() {
        observeVaults = TestObserveVaultsWithItemCount()
        canPerformPaidAction = TestCanPerformPaidAction()
        snackbarDispatcher = TestSnackbarDispatcher()
        bulkMoveToVaultRepository = TestBulkMoveToVaultRepository().apply {
            runBlocking { save(mapOf(SHARE_ID to listOf(ITEM_ID))) }
        }
        savedState = TestSavedStateHandleProvider().apply {
            get()[CommonNavArgId.ShareId.key] = SHARE_ID.id
            get()[MigrateModeArg.key] = MODE.name
            get()[CommonOptionalNavArgId.ItemId.key] = ITEM_ID.id
            get()[MigrateVaultFilterArg.key] = MigrateVaultFilter.All.name
        }
        createViewModel()
    }

    @Test
    fun `emits success when vault selected`() = runTest {
        val (currentVault, otherVault) = initialVaults()
        observeVaults.sendResult(Result.success(listOf(currentVault, otherVault)))

        instance.onVaultSelected(otherVault.vault.shareId)
        instance.state.test {
            val item = awaitItem()
            require(item is MigrateSelectVaultUiState.Success)
            val event = item.event.value()

            assertThat(event).isNotNull()
            assertThat(event!!).isInstanceOf(SelectVaultEvent.VaultSelectedForMigrateItem::class.java)

            val castedEvent = event as SelectVaultEvent.VaultSelectedForMigrateItem
            assertThat(castedEvent.destinationShareId).isEqualTo(otherVault.vault.shareId)

            val bulkMemory = bulkMoveToVaultRepository.observe().first()
            assertThat(bulkMemory.value()).isEqualTo(mapOf(SHARE_ID to listOf(ITEM_ID)))
        }
    }

    @Test
    fun `filters shared vaults when filter mode is set to Shared`() = runTest {
        val sharedVault = VaultWithItemCount(
            vault = TestVault.create(shareId = ShareId("shared-vault"), name = "vault1", shared = true),
            activeItemCount = 1,
            trashedItemCount = 0
        )

        val nonSharedVault = VaultWithItemCount(
            vault = TestVault.create(shareId = ShareId("non-shared-vault"), name = "vault2"),
            activeItemCount = 1,
            trashedItemCount = 0
        )

        observeVaults.sendResult(Result.success(listOf(sharedVault, nonSharedVault)))
        savedState.apply {
            get()[MigrateVaultFilterArg.key] = MigrateVaultFilter.Shared.name
        }
        createViewModel()

        instance.state.test {
            val item = awaitItem()
            require(item is MigrateSelectVaultUiState.Success)

            val expected = VaultEnabledPair(vault = sharedVault, status = VaultStatus.Enabled)
            assertThat(item.vaultList).isEqualTo(listOf(expected))

        }
    }

    private fun createViewModel() {
        instance = MigrateSelectVaultViewModel(
            observeVaults = observeVaults,
            snackbarDispatcher = snackbarDispatcher,
            savedStateHandle = savedState,
            bulkMoveToVaultRepository = bulkMoveToVaultRepository
        )
    }

    private fun initialVaults(): Pair<VaultWithItemCount, VaultWithItemCount> = Pair(
        VaultWithItemCount(
            vault = TestVault.create(shareId = SHARE_ID, name = "vault1"),
            activeItemCount = 1,
            trashedItemCount = 0
        ),
        VaultWithItemCount(
            vault = TestVault.create(shareId = ShareId("OTHER_SHARE_ID"), name = "vault2"),
            activeItemCount = 1,
            trashedItemCount = 0
        )
    )

    companion object {
        private val SHARE_ID = ShareId("123")
        private val ITEM_ID = ItemId("456")

        private val MODE = MigrateModeValue.SelectedItems
    }
}
