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
import proton.android.pass.data.fakes.repositories.TestBulkMoveToVaultRepository
import proton.android.pass.data.fakes.usecases.TestGetVaultWithItemCountById
import proton.android.pass.data.fakes.usecases.TestMigrateItems
import proton.android.pass.data.fakes.usecases.TestMigrateVault
import proton.android.pass.data.fakes.usecases.securelink.FakeObserveHasAssociatedSecureLinks
import proton.android.pass.data.fakes.usecases.shares.FakeObserveShare
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.features.migrate.MigrateModeArg
import proton.android.pass.features.migrate.MigrateModeValue
import proton.android.pass.features.migrate.MigrateSnackbarMessage
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.navigation.api.DestinationShareNavArgId
import proton.android.pass.notifications.fakes.TestSnackbarDispatcher
import proton.android.pass.preferences.TestInternalSettingsRepository
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.TestSavedStateHandle
import proton.android.pass.test.domain.TestVault

internal class MigrateConfirmVaultForMigrateAllVaultItemsViewModelTest {

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var instance: MigrateConfirmVaultViewModel
    private lateinit var migrateItem: TestMigrateItems
    private lateinit var migrateVault: TestMigrateVault
    private lateinit var getVaultById: TestGetVaultWithItemCountById
    private lateinit var snackbarDispatcher: TestSnackbarDispatcher
    private lateinit var bulkMoveToVaultRepository: TestBulkMoveToVaultRepository
    private lateinit var observeHasAssociatedSecureLinks: FakeObserveHasAssociatedSecureLinks
    private lateinit var observeShare: FakeObserveShare
    private lateinit var settingsRepository: TestInternalSettingsRepository

    @Before
    fun setup() {
        migrateItem = TestMigrateItems()
        migrateVault = TestMigrateVault()
        snackbarDispatcher = TestSnackbarDispatcher()
        getVaultById = TestGetVaultWithItemCountById()
        bulkMoveToVaultRepository = TestBulkMoveToVaultRepository()
        observeHasAssociatedSecureLinks = FakeObserveHasAssociatedSecureLinks()
        observeShare = FakeObserveShare()
        settingsRepository = TestInternalSettingsRepository()

        instance = MigrateConfirmVaultViewModel(
            migrateItems = migrateItem,
            migrateVault = migrateVault,
            snackbarDispatcher = snackbarDispatcher,
            getVaultById = getVaultById,
            bulkMoveToVaultRepository = bulkMoveToVaultRepository,
            observeHasAssociatedSecureLinks = observeHasAssociatedSecureLinks,
            savedStateHandle = TestSavedStateHandle.create().apply {
                set(DestinationShareNavArgId.key, DESTINATION_SHARE_ID.id)
                set(CommonOptionalNavArgId.ShareId.key, SHARE_ID.id)
                set(MigrateModeArg.key, MODE.name)
            },
            observeShare = observeShare,
            settingsRepository = settingsRepository
        )
    }

    @Test
    fun `emits initial state`() = runTest {
        instance.state.test {
            val expected = MigrateConfirmVaultUiState.initial(MigrateMode.MigrateAll).copy(
                isLoading = IsLoadingState.Loading // Retrieve vault is loading
            )
            assertThat(awaitItem()).isEqualTo(expected)
        }
    }

    @Test
    fun `can migrate items`() = runTest {
        getVaultById.emitValue(sourceVault())

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

        val expected = TestMigrateVault.Memory(SHARE_ID, DESTINATION_SHARE_ID)
        assertThat(migrateVault.memory()).isEqualTo(listOf(expected))
    }

    @Test
    fun `displays error if cannot migrate items`() = runTest {
        getVaultById.emitValue(sourceVault())
        migrateVault.setResult(Result.failure(IllegalStateException("test")))

        instance.onConfirm()
        instance.state.test {
            val state = awaitItem()
            assertThat(state.isLoading).isInstanceOf(IsLoadingState.NotLoading::class.java)
        }

        val snackbarMessage = snackbarDispatcher.snackbarMessage.first()
        assertThat(snackbarMessage.isNotEmpty()).isTrue()

        val message = snackbarMessage.value()!!
        assertThat(message).isInstanceOf(MigrateSnackbarMessage.VaultItemsNotMigrated::class.java)

        val expected = TestMigrateVault.Memory(SHARE_ID, DESTINATION_SHARE_ID)
        assertThat(migrateVault.memory()).isEqualTo(listOf(expected))

        // No event should have been emitted
        val bulkEvent = bulkMoveToVaultRepository.observeEvent().first()
        assertThat(bulkEvent).isEqualTo(BulkMoveToVaultEvent.Idle)
    }


    private fun sourceVault(): VaultWithItemCount = VaultWithItemCount(
        vault = TestVault.create(shareId = SHARE_ID),
        activeItemCount = 1,
        trashedItemCount = 0
    )

    companion object {
        private val SHARE_ID = ShareId("123")
        private val DESTINATION_SHARE_ID = ShareId("456")

        private val MODE = MigrateModeValue.AllVaultItems
    }
}
