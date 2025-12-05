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
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.common.api.Some
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.fakes.repositories.FakeBulkMoveToVaultRepository
import proton.android.pass.data.fakes.usecases.FakeGetVaultWithItemCountById
import proton.android.pass.data.fakes.usecases.FakeMigrateItems
import proton.android.pass.data.fakes.usecases.FakeMigrateVault
import proton.android.pass.data.fakes.usecases.securelink.FakeObserveHasAssociatedSecureLinks
import proton.android.pass.data.fakes.usecases.shares.FakeObserveShare
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.features.migrate.MigrateModeArg
import proton.android.pass.features.migrate.MigrateModeValue
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.navigation.api.DestinationShareNavArgId
import proton.android.pass.notifications.fakes.FakeSnackbarDispatcher
import proton.android.pass.preferences.FakeInternalSettingsRepository
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.SavedStateHandleTestFactory
import proton.android.pass.test.domain.VaultTestFactory

internal class MigrateConfirmVaultViewModelTest {

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var instance: MigrateConfirmVaultViewModel
    private lateinit var migrateItem: FakeMigrateItems
    private lateinit var migrateVault: FakeMigrateVault
    private lateinit var getVaultById: FakeGetVaultWithItemCountById
    private lateinit var snackbarDispatcher: FakeSnackbarDispatcher
    private lateinit var bulkMoveToVaultRepository: FakeBulkMoveToVaultRepository
    private lateinit var observeHasAssociatedSecureLinks: FakeObserveHasAssociatedSecureLinks
    private lateinit var observeShare: FakeObserveShare
    private lateinit var settingsRepository: FakeInternalSettingsRepository


    @Before
    fun setup() {
        migrateItem = FakeMigrateItems()
        migrateVault = FakeMigrateVault()
        snackbarDispatcher = FakeSnackbarDispatcher()
        getVaultById = FakeGetVaultWithItemCountById()
        bulkMoveToVaultRepository = FakeBulkMoveToVaultRepository()
        observeHasAssociatedSecureLinks = FakeObserveHasAssociatedSecureLinks()
        observeShare = FakeObserveShare()
        settingsRepository = FakeInternalSettingsRepository()

        instance = MigrateConfirmVaultViewModel(
            migrateItems = migrateItem,
            migrateVault = migrateVault,
            snackbarDispatcher = snackbarDispatcher,
            getVaultById = getVaultById,
            bulkMoveToVaultRepository = bulkMoveToVaultRepository,
            observeHasAssociatedSecureLinks = observeHasAssociatedSecureLinks,
            savedStateHandle = SavedStateHandleTestFactory.create().apply {
                set(CommonNavArgId.ShareId.key, SHARE_ID.id)
                set(DestinationShareNavArgId.key, DESTINATION_SHARE_ID.id)
                set(MigrateModeArg.key, MODE.name)
                set(CommonOptionalNavArgId.ItemId.key, ITEM_ID.id)
            },
            observeShare = observeShare,
            settingsRepository = settingsRepository
        )
    }

    @Test
    fun `stops loading when vault has emitted`() = runTest {
        val vault = sourceVault()
        getVaultById.emitValue(vault)
        instance.state.test {
            val secondState = awaitItem()
            assertThat(secondState.isLoading).isInstanceOf(IsLoadingState.NotLoading::class.java)
            assertThat(secondState.vault.isNotEmpty()).isTrue()

            val itemVault = secondState.vault.value()!!
            assertThat(itemVault).isEqualTo(vault)
        }
    }

    @Test
    fun `emits close if there is an error in get vault`() = runTest {
        getVaultById.sendException(IllegalStateException("test"))
        instance.state.test {
            val state = awaitItem()
            assertThat(state.event.isNotEmpty()).isTrue()

            val eventCasted = state.event as Some<ConfirmMigrateEvent>
            assertThat(eventCasted.value).isInstanceOf(ConfirmMigrateEvent.Close::class.java)
        }
    }

    @Test
    fun `emits close if cancel is clicked`() = runTest {
        getVaultById.emitValue(sourceVault())
        instance.onCancel()
        instance.state.test {
            val state = awaitItem()
            val eventCasted = state.event as Some<ConfirmMigrateEvent>
            assertThat(eventCasted.value).isInstanceOf(ConfirmMigrateEvent.Close::class.java)

            cancelAndConsumeRemainingEvents()
        }
    }

    private fun sourceVault(): VaultWithItemCount = VaultWithItemCount(
        vault = VaultTestFactory.create(shareId = SHARE_ID),
        activeItemCount = 1,
        trashedItemCount = 0
    )

    companion object {
        private val SHARE_ID = ShareId("123")
        private val DESTINATION_SHARE_ID = ShareId("456")
        private val ITEM_ID = ItemId("789")

        private val MODE = MigrateModeValue.SelectedItems
    }
}
