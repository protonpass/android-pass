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

package proton.android.pass.features.vault.leave

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.commonui.fakes.FakeSavedStateHandleProvider
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.fakes.usecases.FakeGetVaultByShareId
import proton.android.pass.data.fakes.usecases.FakeLeaveShare
import proton.android.pass.domain.Vault
import proton.android.pass.features.vault.VaultSnackbarMessage
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.fakes.FakeSnackbarDispatcher
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.TestVault

class LeaveShareViewModelTest {

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var instance: LeaveVaultViewModel
    private lateinit var getVaultById: FakeGetVaultByShareId
    private lateinit var leaveVault: FakeLeaveShare
    private lateinit var snackbarDispatcher: FakeSnackbarDispatcher

    @Before
    fun setup() {
        getVaultById = FakeGetVaultByShareId()
        leaveVault = FakeLeaveShare()
        snackbarDispatcher = FakeSnackbarDispatcher()
        instance = LeaveVaultViewModel(
            getVaultByShareId = getVaultById,
            leaveShare = leaveVault,
            savedStateHandle = FakeSavedStateHandleProvider().apply {
                get()[CommonNavArgId.ShareId.key] = "123"
            },
            snackbarDispatcher = snackbarDispatcher
        )
    }

    @Test
    fun `emits initial state`() = runTest {
        instance.state.test {
            assertThat(awaitItem()).isEqualTo(LeaveVaultUiState.Initial)
        }
    }

    @Test
    fun `emits right vault name`() = runTest {
        performSetup()
        instance.state.test {
            val item = awaitItem()
            assertThat(item.isLoadingState).isEqualTo(IsLoadingState.NotLoading)
            assertThat(item.vaultName).isEqualTo(VAULT_NAME)
        }
    }

    @Test
    fun `emits success on leave success`() = runTest {
        performSetup()

        instance.onLeave()
        instance.state.test {
            val item = awaitItem()
            assertThat(item.isLoadingState).isEqualTo(IsLoadingState.NotLoading)
            assertThat(item.event).isEqualTo(LeaveVaultEvent.Left)
        }

        val message = snackbarDispatcher.snackbarMessage.first().value()!!
        assertThat(message).isEqualTo(VaultSnackbarMessage.LeaveVaultSuccess)
    }

    @Test
    fun `emits error on leave error`() = runTest {
        performSetup()

        leaveVault.setResult(Result.failure(IllegalStateException("test")))
        instance.onLeave()
        instance.state.test {
            val item = awaitItem()
            assertThat(item.isLoadingState).isEqualTo(IsLoadingState.NotLoading)
            assertThat(item.event).isEqualTo(LeaveVaultEvent.Unknown)
        }

        val message = snackbarDispatcher.snackbarMessage.first().value()!!
        assertThat(message).isEqualTo(VaultSnackbarMessage.LeaveVaultError)
    }

    @Test
    fun `emits close on getVaultById error`() = runTest {
        getVaultById.sendException(IllegalStateException("test"))
        instance.state.test {
            val item = awaitItem()
            assertThat(item.isLoadingState).isEqualTo(IsLoadingState.NotLoading)
            assertThat(item.event).isEqualTo(LeaveVaultEvent.Close)
        }

        val message = snackbarDispatcher.snackbarMessage.first().value()!!
        assertThat(message).isEqualTo(VaultSnackbarMessage.CannotRetrieveVaultError)
    }

    private fun performSetup() {
        getVaultById.emitValue(testVault())
    }

    private fun testVault(): Vault = TestVault.create(name = VAULT_NAME)

    companion object {
        private const val VAULT_NAME = "vault"
    }

}
