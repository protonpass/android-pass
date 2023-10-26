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

package proton.android.pass.featuresharing.impl.sharingwith

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.account.fakes.TestAccountManager
import proton.android.pass.commonrust.fakes.TestEmailValidator
import proton.android.pass.commonui.fakes.TestSavedStateHandleProvider
import proton.android.pass.data.api.usecases.InviteUserMode
import proton.android.pass.data.fakes.usecases.TestGetInviteUserMode
import proton.android.pass.data.fakes.usecases.TestGetVaultById
import proton.android.pass.featuresharing.impl.SharingWithUserModeType
import proton.android.pass.featuresharing.impl.sharingwith.EmailNotValidReason.NotValid
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.test.MainDispatcherRule
import proton.pass.domain.ShareId
import proton.pass.domain.Vault

class SharingWithViewModelTest {

    private lateinit var viewModel: SharingWithViewModel
    private lateinit var getVaultById: TestGetVaultById
    private lateinit var accountManager: TestAccountManager
    private lateinit var emailValidator: TestEmailValidator
    private lateinit var getInviteUserMode: TestGetInviteUserMode
    private lateinit var savedStateHandleProvider: TestSavedStateHandleProvider

    @get:Rule
    val dispatcherRule = MainDispatcherRule()


    @Before
    fun setUp() {
        getVaultById = TestGetVaultById()
        accountManager = TestAccountManager()
        getInviteUserMode = TestGetInviteUserMode()
        emailValidator = TestEmailValidator()
        savedStateHandleProvider = TestSavedStateHandleProvider().apply {
            get()[CommonNavArgId.ShareId.key] = SHARE_ID
        }
        viewModel = SharingWithViewModel(
            getVaultById = getVaultById,
            accountManager = accountManager,
            savedStateHandleProvider = savedStateHandleProvider,
            getInviteUserMode = getInviteUserMode,
            emailValidator = emailValidator
        )
    }

    @Test
    fun `onEmailChange should update emailState correctly`() = runTest {
        viewModel.onEmailChange("test@example.com")
        viewModel.state.test {
            assertThat(awaitItem().email).isEqualTo("test@example.com")
        }
    }

    @Test
    fun `onEmailSubmit with valid email should update emailNotValidReason to null`() = runTest {
        accountManager.sendPrimaryUserId(UserId("primary-user-id"))
        viewModel.onEmailChange("test@example.com")
        viewModel.onEmailSubmit()
        viewModel.state.test {
            assertThat(awaitItem().emailNotValidReason).isNull()
        }
    }

    @Test
    fun `onEmailSubmit with invalid email should update emailNotValidReason to NotValid`() =
        runTest {
            accountManager.sendPrimaryUserId(UserId("primary-user-id"))
            viewModel.onEmailChange("invalid-email")
            emailValidator.setResult(false)
            viewModel.state.test {
                skipItems(1)
                viewModel.onEmailSubmit()
                assertThat(awaitItem().emailNotValidReason).isEqualTo(NotValid)
            }
        }

    @Test
    fun `onEmailSubmit with not Proton email should navigate to permissions with NewUser`() =
        runTest {
            val invitedEmail = "test@example.com"
            accountManager.sendPrimaryUserId(UserId("primary-user-id"))
            getInviteUserMode.setResult(Result.success(InviteUserMode.NewUser))
            viewModel.onEmailChange(invitedEmail)
            viewModel.state.test {
                skipItems(1)
                viewModel.onEmailSubmit()
                val item = awaitItem()
                assertThat(item.emailNotValidReason).isNull()
                assertThat(item.event).isEqualTo(
                    SharingWithEvents.NavigateToPermissions(
                        shareId = ShareId(SHARE_ID),
                        email = invitedEmail,
                        userMode = SharingWithUserModeType.NewUser
                    )
                )
            }
        }

    @Test
    fun `error in getInviteUserMode should propagate`() =
        runTest {
            val invitedEmail = "test@example.com"
            accountManager.sendPrimaryUserId(UserId("primary-user-id"))
            getInviteUserMode.setResult(Result.failure(IllegalStateException("test")))
            viewModel.onEmailChange(invitedEmail)
            viewModel.state.test {
                skipItems(1)
                viewModel.onEmailSubmit()
                val item = awaitItem()
                assertThat(item.emailNotValidReason).isEqualTo(EmailNotValidReason.CannotGetEmailInfo)
                assertThat(item.event).isEqualTo(SharingWithEvents.Unknown)
            }
        }

    @Test
    fun `state should be updated correctly after combining flows`() = runTest {
        val invitedEmail = "myemail@proton.me"

        accountManager.sendPrimaryUserId(UserId("primary-user-id"))
        val testVault = Vault(
            shareId = ShareId(id = SHARE_ID),
            name = "vault name",
            isPrimary = false
        )
        getVaultById.emitValue(testVault)
        viewModel.onEmailChange(invitedEmail)

        viewModel.onEmailSubmit()
        viewModel.state.test {
            skipItems(1)
            val currentState = awaitItem()
            assertThat(currentState.email).isEqualTo(invitedEmail)
            assertThat(currentState.vault).isEqualTo(testVault)
            assertThat(currentState.emailNotValidReason).isNull()
            assertThat(currentState.isVaultNotFound).isFalse()
            assertThat(currentState.event).isEqualTo(
                SharingWithEvents.NavigateToPermissions(
                    shareId = ShareId(SHARE_ID),
                    email = invitedEmail,
                    userMode = SharingWithUserModeType.ExistingUser
                )
            )
        }
    }

    companion object {
        private const val SHARE_ID = "SharingWithViewModelTest-ShareID"
    }
}
