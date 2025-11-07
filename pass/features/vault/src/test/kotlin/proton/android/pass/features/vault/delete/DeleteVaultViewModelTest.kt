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

package proton.android.pass.features.vault.delete

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.data.fakes.usecases.FakeObserveEncryptedItems
import proton.android.pass.data.fakes.usecases.TestDeleteVault
import proton.android.pass.data.fakes.usecases.TestGetVaultByShareId
import proton.android.pass.features.vault.VaultSnackbarMessage
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.fakes.TestSnackbarDispatcher
import proton.android.pass.preferences.TestInternalSettingsRepository
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.TestSavedStateHandle
import proton.android.pass.test.domain.TestVault
import proton.android.pass.account.fakes.TestAccountManager
import proton.android.pass.navigation.api.IsLastVault

class DeleteVaultViewModelTest {

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var instance: DeleteVaultViewModel
    private lateinit var getVaultById: TestGetVaultByShareId
    private lateinit var deleteVault: TestDeleteVault
    private lateinit var snackbarDispatcher: TestSnackbarDispatcher
    private lateinit var observeEncryptedItems: FakeObserveEncryptedItems

    @Before
    fun setup() {
        getVaultById = TestGetVaultByShareId()
        deleteVault = TestDeleteVault()
        snackbarDispatcher = TestSnackbarDispatcher()
        observeEncryptedItems = FakeObserveEncryptedItems()

        instance = DeleteVaultViewModel(
            getVaultByShareId = getVaultById,
            deleteVault = deleteVault,
            savedStateHandle = TestSavedStateHandle.create().apply {
                set(CommonNavArgId.ShareId.key, "123")
                set(IsLastVault.key, false)
            },
            snackbarDispatcher = snackbarDispatcher,
            observeEncryptedItems = observeEncryptedItems,
            internalSettingsRepository = TestInternalSettingsRepository(),
            accountManager = TestAccountManager()
        )
    }

    @Test
    fun `emits initial state`() = runTest {
        instance.state.test {
            assertThat(awaitItem()).isEqualTo(DeleteVaultUiState.Initial)
        }
    }

    @Test
    fun `emits right vault name`() = runTest {
        performSetup()

        instance.state.test {
            val item = awaitItem()
            assertThat(item.isLoading).isFalse()
            assertThat(item.isButtonEnabled).isEqualTo(IsButtonEnabled.Disabled)
            assertThat(item.vaultName).isEqualTo(VAULT_NAME)
            assertThat(item.vaultText).isEqualTo("")
        }
    }

    @Test
    fun `holds text changes`() = runTest {
        performSetup()

        val text = "1234"
        instance.onTextChange(text)
        instance.state.test {
            val item = awaitItem()
            assertThat(item.vaultText).isEqualTo(text)
        }
    }

    @Test
    fun `allows to delete when text matches vault name`() = runTest {
        performSetup()

        instance.onTextChange(VAULT_NAME)
        instance.state.test {
            val item = awaitItem()
            assertThat(item.vaultText).isEqualTo(VAULT_NAME)
            assertThat(item.isButtonEnabled).isEqualTo(IsButtonEnabled.Enabled)
        }
    }

    @Test
    fun `emits success on delete success`() = runTest {
        performSetup()

        deleteVault.setResult(Result.success(Unit))
        instance.onDelete()
        instance.state.test {
            val item = awaitItem()
            assertThat(item.isLoading).isFalse()
            assertThat(item.event).isEqualTo(DeleteVaultEvent.Deleted)
        }

        val message = snackbarDispatcher.snackbarMessage.first().value()!!
        assertThat(message).isEqualTo(VaultSnackbarMessage.DeleteVaultSuccess)
    }

    @Test
    fun `emits error on delete error`() = runTest {
        performSetup()

        deleteVault.setResult(Result.failure(IllegalStateException("test")))
        instance.onDelete()
        instance.state.test {
            val item = awaitItem()
            assertThat(item.isLoading).isFalse()
            assertThat(item.event).isEqualTo(DeleteVaultEvent.Unknown)
        }

        val message = snackbarDispatcher.snackbarMessage.first().value()!!
        assertThat(message).isEqualTo(VaultSnackbarMessage.DeleteVaultError)
    }

    private fun performSetup() {
        getVaultById.emitValue(TestVault.create(name = VAULT_NAME))
        observeEncryptedItems.emitValue(emptyList())
        instance.onStart()
    }

    companion object {
        private const val VAULT_NAME = "vault"
    }

}
