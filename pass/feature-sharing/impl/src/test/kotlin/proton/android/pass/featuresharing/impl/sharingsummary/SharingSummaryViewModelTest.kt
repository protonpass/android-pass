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

package proton.android.pass.featuresharing.impl.sharingsummary

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.commonui.fakes.TestSavedStateHandleProvider
import proton.android.pass.data.fakes.usecases.TestGetVaultWithItemCountById
import proton.android.pass.data.fakes.usecases.TestInviteToVault
import proton.android.pass.featuresharing.impl.EmailNavArgId
import proton.android.pass.featuresharing.impl.PermissionNavArgId
import proton.android.pass.featuresharing.impl.SharingSnackbarMessage.InviteSentError
import proton.android.pass.featuresharing.impl.SharingSnackbarMessage.InviteSentSuccess
import proton.android.pass.featuresharing.impl.SharingSnackbarMessage.VaultNotFound
import proton.android.pass.featuresharing.impl.SharingWithUserModeArgId
import proton.android.pass.featuresharing.impl.SharingWithUserModeType
import proton.android.pass.featuresharing.impl.sharingpermissions.SharingType
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.fakes.TestSnackbarDispatcher
import proton.android.pass.test.MainDispatcherRule
import proton.pass.domain.ShareColor
import proton.pass.domain.ShareIcon
import proton.pass.domain.ShareId
import proton.pass.domain.Vault
import proton.pass.domain.VaultWithItemCount

class SharingSummaryViewModelTest {

    private lateinit var viewModel: SharingSummaryViewModel
    private lateinit var getVaultWithItemCountById: TestGetVaultWithItemCountById
    private lateinit var inviteToVault: TestInviteToVault
    private lateinit var snackbarDispatcher: TestSnackbarDispatcher
    private lateinit var savedStateHandleProvider: TestSavedStateHandleProvider

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val email = "myemail@proton.me"

    @Before
    fun setUp() {
        getVaultWithItemCountById = TestGetVaultWithItemCountById()
        inviteToVault = TestInviteToVault()
        snackbarDispatcher = TestSnackbarDispatcher()
        savedStateHandleProvider = TestSavedStateHandleProvider().apply {
            get()[CommonNavArgId.ShareId.key] = "my share id"
            get()[EmailNavArgId.key] = email
            get()[PermissionNavArgId.key] = 1
            get()[SharingWithUserModeArgId.key] = SharingWithUserModeType.ExistingUser.name
        }
        viewModel = SharingSummaryViewModel(
            getVaultWithItemCountById = getVaultWithItemCountById,
            inviteToVault = inviteToVault,
            snackbarDispatcher = snackbarDispatcher,
            savedStateHandleProvider = savedStateHandleProvider
        )
    }

    @Test
    fun `test view model initialization`() = runTest {
        viewModel.state.test {
            val initialState = awaitItem()
            assertThat(initialState.email).isEqualTo(email)
            assertThat(initialState.sharingType).isEqualTo(SharingType.Write)
            assertThat(initialState.vaultWithItemCount).isNull()
            assertThat(initialState.isLoading).isTrue()
        }
    }

    @Test
    fun `test view model state with successful vault loading`() = runTest {
        val vaultData = createVaultWithItemCount()
        getVaultWithItemCountById.emitValue(vaultData)

        viewModel.state.test {
            val initialState = awaitItem()
            val expectedState = SharingSummaryUIState(
                email = email,
                vaultWithItemCount = vaultData,
                sharingType = SharingType.Write,
                isLoading = false,
            )
            assertThat(initialState).isEqualTo(expectedState)
        }
    }


    @Test
    fun `test view model state with error in vault loading`() = runTest {
        getVaultWithItemCountById.sendException(RuntimeException("test exception"))

        viewModel.state.test {
            val initialState = awaitItem()
            val expectedState = SharingSummaryUIState(
                email = email,
                vaultWithItemCount = null,
                sharingType = SharingType.Write,
                isLoading = false,
            )
            assertThat(initialState).isEqualTo(expectedState)
        }
        val message = snackbarDispatcher.snackbarMessage.first().value()
        assertThat(message).isNotNull()
        assertThat(message).isEqualTo(VaultNotFound)
    }

    @Test
    fun `test view model onSubmit with null shareId`() = runTest {

        viewModel.onSubmit(email, null, SharingType.Read)

        val message = snackbarDispatcher.snackbarMessage.first().value()
        assertThat(message).isNotNull()
        assertThat(message).isEqualTo(VaultNotFound)
    }

    @Test
    fun `test view model onSubmit success`() = runTest {
        inviteToVault.setResult(Result.success(Unit))

        viewModel.onSubmit(email, ShareId("my share id"), SharingType.Read)

        val message = snackbarDispatcher.snackbarMessage.first().value()
        assertThat(message).isNotNull()
        assertThat(message).isEqualTo(InviteSentSuccess)
    }


    @Test
    fun `test view model onSubmit failure`() = runTest {
        inviteToVault.setResult(Result.failure(RuntimeException("test exception")))

        viewModel.onSubmit(email, ShareId("my share id"), SharingType.Read)

        val message = snackbarDispatcher.snackbarMessage.first().value()
        assertThat(message).isNotNull()
        assertThat(message).isEqualTo(InviteSentError)
    }
}

private fun createVaultWithItemCount() = VaultWithItemCount(
    vault = Vault(
        shareId = ShareId(id = "sociis"),
        name = "Evangeline Potter",
        color = ShareColor.Color1,
        icon = ShareIcon.Icon1,
    ),
    activeItemCount = 5521, trashedItemCount = 6902
)
