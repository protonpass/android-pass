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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.common.api.some
import proton.android.pass.commonrust.fakes.TestEmailValidator
import proton.android.pass.commonui.fakes.TestSavedStateHandleProvider
import proton.android.pass.data.api.repositories.AddressPermission
import proton.android.pass.data.fakes.repositories.TestBulkInviteRepository
import proton.android.pass.data.fakes.usecases.FakeObserveInviteRecommendations
import proton.android.pass.data.fakes.usecases.TestObserveVaultById
import proton.android.pass.domain.InviteRecommendations
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole
import proton.android.pass.domain.Vault
import proton.android.pass.featuresharing.impl.ShowEditVaultArgId
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.test.MainDispatcherRule

class SharingWithViewModelTest {

    private lateinit var viewModel: SharingWithViewModel
    private lateinit var observeVaultById: TestObserveVaultById
    private lateinit var emailValidator: TestEmailValidator
    private lateinit var observeInviteRecommendations: FakeObserveInviteRecommendations
    private lateinit var savedStateHandleProvider: TestSavedStateHandleProvider
    private lateinit var bulkInviteRepository: TestBulkInviteRepository

    @get:Rule
    val dispatcherRule = MainDispatcherRule()


    @Before
    fun setUp() {
        observeVaultById = TestObserveVaultById()
        emailValidator = TestEmailValidator()
        observeInviteRecommendations = FakeObserveInviteRecommendations()
        bulkInviteRepository = TestBulkInviteRepository()
        savedStateHandleProvider = TestSavedStateHandleProvider().apply {
            get()[CommonNavArgId.ShareId.key] = SHARE_ID
            get()[ShowEditVaultArgId.key] = false
        }
        viewModel = SharingWithViewModel(
            observeVaultById = observeVaultById,
            savedStateHandleProvider = savedStateHandleProvider,
            emailValidator = emailValidator,
            observeInviteRecommendations = observeInviteRecommendations,
            bulkInviteRepository = bulkInviteRepository
        )
    }

    @Test
    fun `onEmailChange should update emailState correctly`() = runTest {
        val testEmail = "test@email.test"
        viewModel.onEmailChange(testEmail)
        assertThat(viewModel.editingEmail).isEqualTo(testEmail)
    }

    @Test
    fun `onEmailSubmit with valid email should update emailNotValidReason to null`() = runTest {
        viewModel.onEmailChange("test@example.com")
        viewModel.onEmailSubmit()
        viewModel.state.test {
            assertThat(awaitItem().showEmailNotValidError).isFalse()
        }
    }

    @Test
    fun `onEmailSubmit with valid email should add the email to the list`() = runTest {
        val email = "test@example.com"
        viewModel.onEmailChange(email)
        viewModel.onEmailSubmit()
        viewModel.state.test {
            assertThat(awaitItem().enteredEmails).isEqualTo(listOf(email))
        }
        assertThat(viewModel.editingEmail).isEmpty()
    }

    @Test
    fun `onContinueClick with valid email should send it to repository`() = runTest {
        val email = "test@example.com"
        viewModel.onEmailChange(email)
        viewModel.onEmailSubmit()
        viewModel.onContinueClick()

        val memory = bulkInviteRepository.observeAddresses().first()
        assertThat(memory).isEqualTo(listOf(AddressPermission(email, ShareRole.Read)))
    }

    @Test
    fun `onEmailSubmit with invalid email should update emailNotValidReason to NotValid`() =
        runTest {
            viewModel.onEmailChange("invalid-email")
            emailValidator.setResult(false)
            viewModel.state.test {
                skipItems(1)
                viewModel.onEmailSubmit()
                assertThat(awaitItem().showEmailNotValidError).isTrue()
            }
        }

    @Test
    fun `state should be updated correctly after combining flows`() = runTest {
        val invitedEmail = "myemail@proton.me"

        val testVault = Vault(
            shareId = ShareId(id = SHARE_ID),
            name = "vault name",
        )
        observeVaultById.emitValue(testVault.some())
        viewModel.onEmailChange(invitedEmail)

        viewModel.onEmailSubmit()
        viewModel.onContinueClick()
        viewModel.state.test {
            val currentState = awaitItem()
            assertThat(currentState.vault).isEqualTo(testVault)
            assertThat(currentState.showEmailNotValidError).isFalse()
            assertThat(currentState.event).isEqualTo(
                SharingWithEvents.NavigateToPermissions(shareId = ShareId(SHARE_ID))
            )
        }

        val addresses = bulkInviteRepository.observeAddresses().first()
        assertThat(addresses.size).isEqualTo(1)
        assertThat(addresses[0].address).isEqualTo(invitedEmail)
    }

    @Test
    fun `double click on entered email should remove it from the list`() = runTest {
        val email1 = "test@email.test"
        val email2 = "another@email.test"
        val recommendations = InviteRecommendations(
            recommendedEmails = listOf(email1, email2),
            planInternalName = "",
            groupDisplayName = "",
            planRecommendedEmails = emptyList()
        )
        observeInviteRecommendations.emitInvites(recommendations)

        viewModel.onItemToggle(email1, false)

        viewModel.state.test {
            val state = awaitItem()
            assertThat(state.enteredEmails).isEqualTo(listOf(email1))

            val suggestionsState = state.suggestionsUIState
            assertThat(suggestionsState).isInstanceOf(SuggestionsUIState.Content::class.java)

            val content = suggestionsState as SuggestionsUIState.Content
            assertThat(content.recentEmails.size).isEqualTo(2)
            assertThat(content.recentEmails).contains(email1 to true)
            assertThat(content.recentEmails).contains(email2 to false)
        }

        viewModel.onEmailClick(0)
        viewModel.onEmailClick(0)

        viewModel.state.test {
            val state = awaitItem()
            assertThat(state.enteredEmails).isEmpty()

            val suggestionsState = state.suggestionsUIState
            assertThat(suggestionsState).isInstanceOf(SuggestionsUIState.Content::class.java)

            val content = suggestionsState as SuggestionsUIState.Content
            assertThat(content.recentEmails.size).isEqualTo(2)
            assertThat(content.recentEmails).contains(email1 to false)
            assertThat(content.recentEmails).contains(email2 to false)
        }
    }

    companion object {
        private const val SHARE_ID = "SharingWithViewModelTest-ShareID"
    }
}
