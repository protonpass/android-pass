/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.featurevault.impl.leave

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.commonui.fakes.TestSavedStateHandleProvider
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.fakes.usecases.TestGetVaultById
import proton.android.pass.data.fakes.usecases.TestLeaveVault
import proton.android.pass.featurevault.impl.VaultSnackbarMessage
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.fakes.TestSnackbarDispatcher
import proton.android.pass.test.MainDispatcherRule
import proton.pass.domain.ShareColor
import proton.pass.domain.ShareIcon
import proton.pass.domain.ShareId
import proton.pass.domain.Vault

class LeaveVaultViewModelTest {

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var instance: LeaveVaultViewModel
    private lateinit var getVaultById: TestGetVaultById
    private lateinit var leaveVault: TestLeaveVault
    private lateinit var snackbarDispatcher: TestSnackbarDispatcher

    @Before
    fun setup() {
        getVaultById = TestGetVaultById()
        leaveVault = TestLeaveVault()
        snackbarDispatcher = TestSnackbarDispatcher()
        instance = LeaveVaultViewModel(
            getVaultById = getVaultById,
            leaveVault = leaveVault,
            savedStateHandle = TestSavedStateHandleProvider().apply {
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

    private fun testVault(): Vault = Vault(
        shareId = ShareId("123"),
        name = VAULT_NAME,
        color = ShareColor.Color2,
        icon = ShareIcon.Icon1,
        isPrimary = false
    )

    companion object {
        private const val VAULT_NAME = "vault"
    }

}
