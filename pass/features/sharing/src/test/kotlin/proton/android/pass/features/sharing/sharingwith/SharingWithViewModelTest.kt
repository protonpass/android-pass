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

package proton.android.pass.features.sharing.sharingwith

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.common.api.some
import proton.android.pass.commonrust.fakes.TestEmailValidator
import proton.android.pass.commonui.fakes.TestSavedStateHandleProvider
import proton.android.pass.data.api.repositories.AddressPermission
import proton.android.pass.data.api.usecases.CanAddressesBeInvitedResult
import proton.android.pass.data.fakes.repositories.TestBulkInviteRepository
import proton.android.pass.data.fakes.usecases.FakeObserveInviteRecommendations
import proton.android.pass.data.fakes.usecases.TestCheckAddressesCanBeInvited
import proton.android.pass.data.fakes.usecases.TestObserveOrganizationSettings
import proton.android.pass.data.fakes.usecases.TestObserveVaultById
import proton.android.pass.domain.InviteRecommendations
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole
import proton.android.pass.features.sharing.ShowEditVaultArgId
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.TestVault

class SharingWithViewModelTest {

    private lateinit var viewModel: SharingWithViewModel
    private lateinit var observeVaultById: TestObserveVaultById
    private lateinit var emailValidator: TestEmailValidator
    private lateinit var observeInviteRecommendations: FakeObserveInviteRecommendations
    private lateinit var savedStateHandleProvider: TestSavedStateHandleProvider
    private lateinit var bulkInviteRepository: TestBulkInviteRepository
    private lateinit var checkAddressesCanBeInvited: TestCheckAddressesCanBeInvited

    @get:Rule
    val dispatcherRule = MainDispatcherRule()


    @Before
    fun setUp() {
        observeVaultById = TestObserveVaultById()
        emailValidator = TestEmailValidator()
        observeInviteRecommendations = FakeObserveInviteRecommendations()
        bulkInviteRepository = TestBulkInviteRepository()
        checkAddressesCanBeInvited = TestCheckAddressesCanBeInvited()
        savedStateHandleProvider = TestSavedStateHandleProvider().apply {
            get()[CommonNavArgId.ShareId.key] = SHARE_ID
            get()[ShowEditVaultArgId.key] = false
        }
        viewModel = SharingWithViewModel(
            observeVaultById = observeVaultById,
            savedStateHandleProvider = savedStateHandleProvider,
            emailValidator = emailValidator,
            observeInviteRecommendations = observeInviteRecommendations,
            bulkInviteRepository = bulkInviteRepository,
            observeOrganizationSettings = TestObserveOrganizationSettings(),
            checkCanAddressesBeInvited = checkAddressesCanBeInvited
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
        viewModel.stateFlow.test {
            assertThat(awaitItem().errorMessage == ErrorMessage.EmailNotValid).isFalse()
        }
    }

    @Test
    fun `onEmailSubmit with valid email should add the email to the list`() = runTest {
        val email = "test@example.com"
        checkAddressesCanBeInvited.setAddressCanBeInvited(email)

        viewModel.onEmailChange(email)
        viewModel.onEmailSubmit()
        viewModel.stateFlow.test {
            val stateEmails = awaitItem().enteredEmails
            val expected = EnteredEmailState(email = email, isError = false)
            assertThat(stateEmails).isEqualTo(listOf(expected))
        }
        assertThat(viewModel.editingEmail).isEmpty()
    }

    @Test
    fun `onContinueClick with valid email should send it to repository`() = runTest {
        val email = "test@example.com"
        checkAddressesCanBeInvited.setAddressCanBeInvited(email)

        viewModel.onEmailChange(email)
        viewModel.onEmailSubmit()
        viewModel.onContinueClick()

        val memory = bulkInviteRepository.observeAddresses().first()
        assertThat(memory).isEqualTo(listOf(AddressPermission(email, ShareRole.Read)))
    }

    @Test
    fun `onEmailSubmit with invalid email should update emailNotValidReason to NotValid`() = runTest {
        viewModel.onEmailChange("invalid-email")
        emailValidator.setResult(false)
        viewModel.stateFlow.test {
            skipItems(1)
            viewModel.onEmailSubmit()
            assertThat(awaitItem().errorMessage == ErrorMessage.EmailNotValid).isTrue()
        }
    }

    @Test
    fun `state should be updated correctly after combining flows`() = runTest {
        val invitedEmail = "myemail@proton.me"
        checkAddressesCanBeInvited.setAddressCanBeInvited(invitedEmail)

        val testVault = TestVault.create(
            shareId = ShareId(id = SHARE_ID)
        )
        observeVaultById.emitValue(testVault.some())
        viewModel.onEmailChange(invitedEmail)

        viewModel.onEmailSubmit()
        viewModel.onContinueClick()
        viewModel.stateFlow.test {
            val currentState = awaitItem()
            assertThat(currentState.vault).isEqualTo(testVault)
            assertThat(currentState.errorMessage == ErrorMessage.EmailNotValid).isFalse()
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
        val result = CanAddressesBeInvitedResult.All(recommendations.recommendedEmails)
        checkAddressesCanBeInvited.setResult(Result.success(result))
        observeInviteRecommendations.emitInvites(recommendations)

        viewModel.onItemToggle(email1, false)

        viewModel.stateFlow.test {
            val state = awaitItem()
            val expectedEnteredEmails = listOf(EnteredEmailState(email = email1, isError = false))
            assertThat(state.enteredEmails).isEqualTo(expectedEnteredEmails)

            val suggestionsState = state.suggestionsUIState
            assertThat(suggestionsState).isInstanceOf(SuggestionsUIState.Content::class.java)

            val content = suggestionsState as SuggestionsUIState.Content
            assertThat(content.recentEmails.size).isEqualTo(2)

            assertThat(content.recentEmails).contains(email1 to true)
            assertThat(content.recentEmails).contains(email2 to false)
        }

        viewModel.onEmailClick(0)
        viewModel.onEmailClick(0)

        viewModel.stateFlow.test {
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

    @Test
    fun `does not send request if some address cannot be invited`() = runTest {
        val email1 = "test1@email.test"
        val email2 = "test2@email.test"
        val canResult = CanAddressesBeInvitedResult.Some(
            canBe = listOf(email1),
            cannotBe = listOf(email2),
            reason = CanAddressesBeInvitedResult.CannotInviteAddressReason.CannotInviteOutsideOrg
        )
        checkAddressesCanBeInvited.setResult(Result.success(canResult))

        // Setup email1
        viewModel.onEmailChange(email1)
        viewModel.onEmailSubmit()

        // Setup email2
        viewModel.onEmailChange(email2)
        viewModel.onEmailSubmit()

        // Click on continue
        viewModel.onContinueClick()
        viewModel.stateFlow.test {
            val item = awaitItem()
            assertThat(item.errorMessage).isEqualTo(ErrorMessage.SomeAddressesCannotBeInvited)

            val expectedEmails = persistentListOf(
                EnteredEmailState(email = email1, isError = false),
                EnteredEmailState(email = email2, isError = true)
            )
            assertThat(item.enteredEmails).isEqualTo(expectedEmails)
        }

        // Assert it has not gone through
        val addresses = bulkInviteRepository.observeAddresses().first()
        assertThat(addresses).isEmpty()
    }

    @Test
    fun `shows error if the current address has already been added`() = runTest {
        val email = "test1@email.test"
        viewModel.onEmailChange(email)
        viewModel.onEmailSubmit()
        viewModel.onEmailChange(email)
        viewModel.onEmailSubmit()

        viewModel.stateFlow.test {
            val item = awaitItem()
            assertThat(item.errorMessage).isEqualTo(ErrorMessage.EmailAlreadyAdded)

            // Assert that it does not clean the current state
            assertThat(viewModel.editingEmail).isEqualTo(email)
        }
    }

    @Test
    fun `does not continue if the current address has already been added`() = runTest {
        val email = "test1@email.test"
        viewModel.onEmailChange(email)
        viewModel.onEmailSubmit()
        viewModel.onEmailChange(email)
        viewModel.onContinueClick()

        viewModel.stateFlow.test {
            val item = awaitItem()
            assertThat(item.errorMessage).isEqualTo(ErrorMessage.EmailAlreadyAdded)
            assertThat(item.event).isInstanceOf(SharingWithEvents.Idle::class.java)

            // Assert that it does not clean the current state
            assertThat(viewModel.editingEmail).isEqualTo(email)
        }
    }

    companion object {
        private const val SHARE_ID = "SharingWithViewModelTest-ShareID"
    }
}
