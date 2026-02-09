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
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.commonui.fakes.FakeSavedStateHandleProvider
import proton.android.pass.data.fakes.repositories.FakeBulkMoveToVaultRepository
import proton.android.pass.data.fakes.usecases.FakeObserveVaultsWithItemCount
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.features.migrate.MigrateModeArg
import proton.android.pass.features.migrate.MigrateModeValue
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.fakes.FakeSnackbarDispatcher
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.VaultTestFactory

class MigrateSelectVaultForMigrateAllVaultItemsViewModelTest {

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var instance: MigrateSelectVaultViewModel
    private lateinit var observeVaults: FakeObserveVaultsWithItemCount
    private lateinit var snackbarDispatcher: FakeSnackbarDispatcher
    private lateinit var bulkMoveToVaultRepository: FakeBulkMoveToVaultRepository

    @Before
    fun setup() {
        observeVaults = FakeObserveVaultsWithItemCount()
        snackbarDispatcher = FakeSnackbarDispatcher()
        bulkMoveToVaultRepository = FakeBulkMoveToVaultRepository()
        instance = MigrateSelectVaultViewModel(
            observeVaults = observeVaults,
            snackbarDispatcher = snackbarDispatcher,
            bulkMoveToVaultRepository = bulkMoveToVaultRepository,
            savedStateHandle = FakeSavedStateHandleProvider().apply {
                get()[CommonNavArgId.ShareId.key] = SHARE_ID.id
                get()[MigrateModeArg.key] = MODE.name
            }
        )
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
            assertThat(event!!).isInstanceOf(SelectVaultEvent.VaultSelectedForMigrateAll::class.java)

            val castedEvent = event as SelectVaultEvent.VaultSelectedForMigrateAll
            assertThat(castedEvent.sourceShareId).isEqualTo(SHARE_ID)
            assertThat(castedEvent.destinationShareId).isEqualTo(otherVault.vault.shareId)
        }
    }

    private fun initialVaults(): Pair<VaultWithItemCount, VaultWithItemCount> = Pair(
        VaultWithItemCount(
            vault = VaultTestFactory.create(shareId = SHARE_ID),
            activeItemCount = 1,
            trashedItemCount = 0
        ),
        VaultWithItemCount(
            vault = VaultTestFactory.create(shareId = ShareId("OTHER_SHARE_ID")),
            activeItemCount = 1,
            trashedItemCount = 0
        )
    )

    companion object {
        private val SHARE_ID = ShareId("123")

        private val MODE = MigrateModeValue.AllVaultItems
    }
}
