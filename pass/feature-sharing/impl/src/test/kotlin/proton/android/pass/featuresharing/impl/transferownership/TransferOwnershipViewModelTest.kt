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

package proton.android.pass.featuresharing.impl.transferownership

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.commonui.fakes.TestSavedStateHandleProvider
import proton.android.pass.data.fakes.usecases.TestTransferVaultOwnership
import proton.android.pass.featuresharing.impl.SharingSnackbarMessage
import proton.android.pass.featuresharing.impl.manage.bottomsheet.MemberEmailArg
import proton.android.pass.featuresharing.impl.manage.bottomsheet.MemberShareIdArg
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.fakes.TestSnackbarDispatcher
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.domain.ShareId

class TransferOwnershipViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var instance: TransferOwnershipViewModel

    private lateinit var snackbarDispatcher: TestSnackbarDispatcher
    private lateinit var transferVaultOwnership: TestTransferVaultOwnership

    @Before
    fun setup() {
        snackbarDispatcher = TestSnackbarDispatcher()
        transferVaultOwnership = TestTransferVaultOwnership()
        instance = TransferOwnershipViewModel(
            snackbarDispatcher = snackbarDispatcher,
            transferVaultOwnership = transferVaultOwnership,
            savedState = TestSavedStateHandleProvider().apply {
                get()[CommonNavArgId.ShareId.key] = SHARE_ID
                get()[MemberShareIdArg.key] = MEMBER_SHARE_ID
                get()[MemberEmailArg.key] = MEMBER_EMAIL
            }
        )
    }

    @Test
    fun `emit initial state`() = runTest {
        instance.state.test {
            assertThat(awaitItem()).isEqualTo(TransferOwnershipState.initial(MEMBER_EMAIL))
        }
    }

    @Test
    fun `can transfer ownership`() = runTest {
        instance.transferOwnership()

        instance.state.test {
            assertThat(awaitItem().event).isEqualTo(TransferOwnershipEvent.OwnershipTransferred)
        }

        val memory = transferVaultOwnership.getMemory()
        val expected = TestTransferVaultOwnership.Payload(
            shareId = ShareId(SHARE_ID),
            memberShareId = ShareId(MEMBER_SHARE_ID)
        )
        assertThat(memory).isEqualTo(listOf(expected))

        val message = snackbarDispatcher.snackbarMessage.first().value()
        assertThat(message).isInstanceOf(SharingSnackbarMessage.TransferOwnershipSuccess::class.java)
    }

    @Test
    fun `can handle transfer ownership error`() = runTest {
        transferVaultOwnership.setResult(Result.failure(IllegalStateException("test")))
        instance.transferOwnership()

        instance.state.test {
            assertThat(awaitItem().event).isEqualTo(TransferOwnershipEvent.Unknown)
        }

        val message = snackbarDispatcher.snackbarMessage.first().value()
        assertThat(message).isInstanceOf(SharingSnackbarMessage.TransferOwnershipError::class.java)
    }

    companion object {
        private const val SHARE_ID = "TransferOwnershipViewModelTest-SHARE_ID"
        private const val MEMBER_SHARE_ID = "TransferOwnershipViewModelTest-MEMBER_SHARE_ID"
        private const val MEMBER_EMAIL = "TransferOwnershipViewModelTest-MEMBER_EMAIL"
    }
}
