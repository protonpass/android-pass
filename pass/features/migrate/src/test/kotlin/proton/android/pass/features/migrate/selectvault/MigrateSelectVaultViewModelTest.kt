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
import app.cash.turbine.ReceiveTurbine
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.commonui.fakes.FakeSavedStateHandleProvider
import proton.android.pass.data.fakes.repositories.FakeBulkMoveToVaultRepository
import proton.android.pass.data.fakes.usecases.FakeObserveVaultsWithItemCount
import proton.android.pass.data.fakes.usecases.folders.FakeObserveFolders
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
            runBlocking { save(mapOf(SHARE_ID to listOf(ITEM_ID))) }

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
            VaultEnabledPair(
                vaultWithItemCount = currentVault,
                status = VaultStatus.Disabled(VaultStatus.DisabledReason.SameVault)
            ),
            VaultEnabledPair(otherVault, VaultStatus.Enabled)
        )
        instance.state.test {
            val item = awaitItem()
            require(item is MigrateSelectVaultUiState.Success)
            assertThat(item.vaultList).isEqualTo(expected)
            assertThat(item.event.value()).isNull()
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
    }

}
