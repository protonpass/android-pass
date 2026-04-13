/*
 * Copyright (c) 2023-2026 Proton AG
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

import app.cash.turbine.Event
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.common.api.None
import proton.android.pass.commonrust.fakes.FakeEmailValidator
import proton.android.pass.commonui.fakes.FakeSavedStateHandleProvider
import proton.android.pass.data.api.repositories.UserTarget
import proton.android.pass.data.api.usecases.CanAddressesBeInvitedResult
import proton.android.pass.data.fakes.repositories.FakeBulkInviteRepository
import proton.android.pass.data.fakes.usecases.FakeCheckAddressesCanBeInvited
import proton.android.pass.data.fakes.usecases.FakeGetVaultMembers
import proton.android.pass.data.fakes.usecases.FakeObserveGroupMembersByGroup
import proton.android.pass.data.fakes.usecases.FakeObserveInviteRecommendations
import proton.android.pass.data.fakes.usecases.FakeObserveOrganizationSettings
import proton.android.pass.data.fakes.usecases.shares.FakeObserveShare
import proton.android.pass.data.fakes.usecases.shares.FakeObserveShareItemMembers
import proton.android.pass.data.fakes.usecases.shares.FakeObserveSharePendingInvites
import proton.android.pass.domain.InviteId
import proton.android.pass.domain.ShareRole
import proton.android.pass.domain.shares.SharePendingInvite
import proton.android.pass.domain.GroupId
import proton.android.pass.domain.InviteRecommendations
import proton.android.pass.domain.RecommendedEmail
import proton.android.pass.domain.RecommendedGroup
import proton.android.pass.domain.ShareId
import proton.android.pass.features.sharing.ShowEditVaultArgId
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.ShareTestFactory
import proton.android.pass.test.domain.VaultMemberTestFactory

class SharingWithViewModelTest {

    private lateinit var viewModel: SharingWithViewModel
    private lateinit var observeShare: FakeObserveShare
    private lateinit var emailValidator: FakeEmailValidator
    private lateinit var observeInviteRecommendations: FakeObserveInviteRecommendations
    private lateinit var savedStateHandleProvider: FakeSavedStateHandleProvider
    private lateinit var bulkInviteRepository: FakeBulkInviteRepository
    private lateinit var checkAddressesCanBeInvited: FakeCheckAddressesCanBeInvited
    private lateinit var getVaultMembers: FakeGetVaultMembers
    private lateinit var observeGroupMembersByGroup: FakeObserveGroupMembersByGroup
    private lateinit var observeSharePendingInvites: FakeObserveSharePendingInvites

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Before
    fun setUp() {
        observeShare = FakeObserveShare()
        emailValidator = FakeEmailValidator()
        observeInviteRecommendations = FakeObserveInviteRecommendations()
        bulkInviteRepository = FakeBulkInviteRepository()
        checkAddressesCanBeInvited = FakeCheckAddressesCanBeInvited()
        getVaultMembers = FakeGetVaultMembers()
        observeGroupMembersByGroup = FakeObserveGroupMembersByGroup()
        observeSharePendingInvites = FakeObserveSharePendingInvites().apply { emitValue(emptyList()) }
        savedStateHandleProvider = FakeSavedStateHandleProvider().apply {
            get()[CommonNavArgId.ShareId.key] = SHARE_ID
            get()[ShowEditVaultArgId.key] = false
            get()[CommonOptionalNavArgId.ItemId.key] = null
        }
        viewModel = SharingWithViewModel(
            observeShare = observeShare,
            savedStateHandleProvider = savedStateHandleProvider,
            emailValidator = emailValidator,
            observeInviteRecommendations = observeInviteRecommendations,
            bulkInviteRepository = bulkInviteRepository,
            observeOrganizationSettings = FakeObserveOrganizationSettings(),
            checkCanAddressesBeInvited = checkAddressesCanBeInvited,
            getVaultMembers = getVaultMembers,
            observeGroupMembersByGroup = observeGroupMembersByGroup,
            observeShareItemMembers = FakeObserveShareItemMembers(),
            observeSharePendingInvites = observeSharePendingInvites
        )

        observeShare.emitValue(ShareTestFactory.random())
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
            val expected = EnteredEmailUiModel(email = email, isError = false)
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

        val memory = bulkInviteRepository.observeInvites().first()
        assertThat(memory).isEqualTo(listOf(UserTarget(email, ShareRole.Read)))
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

        val vaultShare = ShareTestFactory.Vault.create(id = SHARE_ID)
        observeShare.emitValue(vaultShare)
        viewModel.onEmailChange(invitedEmail)

        viewModel.onEmailSubmit()
        viewModel.onContinueClick()
        viewModel.stateFlow.test {
            val currentState = awaitItem()
            assertThat(currentState.share).isEqualTo(vaultShare)
            assertThat(currentState.errorMessage == ErrorMessage.EmailNotValid).isFalse()
            assertThat(currentState.event).isEqualTo(
                SharingWithEvents.NavigateToPermissions(
                    shareId = ShareId(SHARE_ID),
                    itemIdOption = None
                )
            )
        }

        val invites = bulkInviteRepository.observeInvites().first()
        assertThat(invites.size).isEqualTo(1)
        assertThat((invites[0] as UserTarget).email).isEqualTo(invitedEmail)
    }

    @Test
    fun `click on entered email chip should remove it from the list`() = runTest {
        val email1 = "test@email.test"
        val email2 = "another@email.test"
        val recommendations = InviteRecommendations(
            recommendedItems = listOf(RecommendedEmail(email1), RecommendedEmail(email2)),
            groupDisplayName = "",
            organizationItems = emptyList()
        )
        val result = CanAddressesBeInvitedResult.All(
            recommendations.recommendedItems.map { it.email }
        )
        checkAddressesCanBeInvited.setResult(Result.success(result))
        observeInviteRecommendations.emitInvites(recommendations)

        viewModel.onItemToggle(email1, false)

        viewModel.stateFlow.test {
            val state = awaitItem()
            val expectedEnteredEmails = listOf(EnteredEmailUiModel(email = email1, isError = false))
            assertThat(state.enteredEmails).isEqualTo(expectedEnteredEmails)

            val suggestionsState = state.suggestionsUIState
            assertThat(suggestionsState).isInstanceOf(SuggestionsUIState.Content::class.java)

            val content = suggestionsState as SuggestionsUIState.Content
            val recentEmails = content.recentSortedItems.filterIsInstance<EmailUiModel>()
            assertThat(recentEmails.size).isEqualTo(2)

            assertThat(recentEmails[0]).isEqualTo(EmailUiModel(email2, false))
            assertThat(recentEmails[1]).isEqualTo(EmailUiModel(email1, true))
        }

        viewModel.onChipEmailClick(0)

        viewModel.stateFlow.test {
            val state = awaitItem()
            assertThat(state.enteredEmails).isEmpty()

            val suggestionsState = state.suggestionsUIState
            assertThat(suggestionsState).isInstanceOf(SuggestionsUIState.Content::class.java)

            val content = suggestionsState as SuggestionsUIState.Content
            val recentEmails = content.recentSortedItems.filterIsInstance<EmailUiModel>()
            assertThat(recentEmails.size).isEqualTo(2)
            assertThat(recentEmails[0]).isEqualTo(EmailUiModel(email2, false))
            assertThat(recentEmails[1]).isEqualTo(EmailUiModel(email1, false))
        }
    }

    @Test
    fun `click on selected group chip should remove it`() = runTest {
        val groupId = GroupId("group-id")
        val recommendations = InviteRecommendations(
            recommendedItems = listOf(
                RecommendedGroup(
                    groupId = groupId,
                    email = "group@proton.test",
                    name = "Team group",
                    memberCount = 3
                )
            ),
            groupDisplayName = "",
            organizationItems = emptyList()
        )
        observeInviteRecommendations.emitInvites(recommendations)

        viewModel.onGroupToggle(groupId = groupId, isSelected = false)

        viewModel.stateFlow.test {
            val state = awaitItem()
            val content = state.suggestionsUIState as SuggestionsUIState.Content
            assertThat(content.recentSortedItems.filterIsInstance<GroupSuggestionUiModel>()).containsExactly(
                GroupSuggestionUiModel(
                    id = groupId,
                    email = "group@proton.test",
                    name = "Team group",
                    memberCount = 3,
                    isSelected = true
                )
            )
        }

        viewModel.onChipGroupRemoveClick(groupId)

        viewModel.stateFlow.test {
            val state = awaitItem()
            assertThat(state.selectedGroups).isEmpty()

            val content = state.suggestionsUIState as SuggestionsUIState.Content
            assertThat(content.recentSortedItems.filterIsInstance<GroupSuggestionUiModel>()).containsExactly(
                GroupSuggestionUiModel(
                    id = groupId,
                    email = "group@proton.test",
                    name = "Team group",
                    memberCount = 3
                )
            )
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
                EnteredEmailUiModel(email = email1, isError = false),
                EnteredEmailUiModel(email = email2, isError = true)
            )
            assertThat(item.enteredEmails).isEqualTo(expectedEmails)
        }

        // Assert it has not gone through
        val invites = bulkInviteRepository.observeInvites().first()
        assertThat(invites).isEmpty()
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

    @Test
    fun `vault member email appears as already invited in suggestions`() = runTest {
        val memberEmail = "member@proton.me"
        getVaultMembers.emitValue(
            listOf(
                VaultMemberTestFactory.Member.create(
                    email = memberEmail,
                    shareId = ShareId(SHARE_ID),
                    username = memberEmail
                )
            )
        )
        observeInviteRecommendations.emitInvites(
            InviteRecommendations(
                recommendedItems = listOf(RecommendedEmail(memberEmail)),
                groupDisplayName = "",
                organizationItems = emptyList()
            )
        )

        viewModel.stateFlow.test {
            val content = awaitItem().suggestionsUIState as SuggestionsUIState.Content
            val emails = content.recentSortedItems.filterIsInstance<EmailUiModel>()
            assertThat(emails).containsExactly(EmailUiModel(memberEmail, isAlreadyMember = true))
        }
    }

    @Test
    fun `pending invite email appears as already invited in suggestions`() = runTest {
        val invitedEmail = "pending@proton.me"
        observeSharePendingInvites.emitValue(
            listOf(
                SharePendingInvite.ExistingUser(
                    email = invitedEmail,
                    inviteId = InviteId("invite-1"),
                    targetId = SHARE_ID
                )
            )
        )
        observeInviteRecommendations.emitInvites(
            InviteRecommendations(
                recommendedItems = listOf(RecommendedEmail(invitedEmail)),
                groupDisplayName = "",
                organizationItems = emptyList()
            )
        )

        viewModel.stateFlow.test {
            val content = awaitItem().suggestionsUIState as SuggestionsUIState.Content
            val emails = content.recentSortedItems.filterIsInstance<EmailUiModel>()
            assertThat(emails).containsExactly(EmailUiModel(invitedEmail, isAlreadyMember = true))
        }
    }

    @Test
    fun `group with vault access appears as already invited in suggestions`() = runTest {
        val groupEmail = "engineering@proton.me"
        val groupId = GroupId("group-eng")
        getVaultMembers.emitValue(
            listOf(
                VaultMemberTestFactory.Group.create(
                    email = groupEmail,
                    shareId = ShareId(SHARE_ID),
                    groupId = groupId,
                    username = groupEmail,
                    memberCount = 3,
                    role = ShareRole.Read
                )
            )
        )
        observeInviteRecommendations.emitInvites(
            InviteRecommendations(
                recommendedItems = listOf(
                    RecommendedGroup(groupId = groupId, email = groupEmail, name = "Engineering", memberCount = 3)
                ),
                groupDisplayName = "",
                organizationItems = emptyList()
            )
        )

        viewModel.stateFlow.test {
            val content = awaitItem().suggestionsUIState as SuggestionsUIState.Content
            val groups = content.recentSortedItems.filterIsInstance<GroupSuggestionUiModel>()
            assertThat(groups).containsExactly(
                GroupSuggestionUiModel(
                    id = groupId,
                    email = groupEmail,
                    name = "Engineering",
                    memberCount = 3,
                    isAlreadyMember = true
                )
            )
        }
    }

    @Test
    fun `non-member email is not marked as already invited`() = runTest {
        val freshEmail = "fresh@proton.me"
        observeInviteRecommendations.emitInvites(
            InviteRecommendations(
                recommendedItems = listOf(RecommendedEmail(freshEmail)),
                groupDisplayName = "",
                organizationItems = emptyList()
            )
        )

        viewModel.stateFlow.test {
            val content = awaitItem().suggestionsUIState as SuggestionsUIState.Content
            val emails = content.recentSortedItems.filterIsInstance<EmailUiModel>()
            assertThat(emails).containsExactly(EmailUiModel(freshEmail, isAlreadyMember = false))
        }
    }

    @Test
    fun `suggestionsUIState does not become Loading when typing after content is loaded`() = runTest {
        val email = "test@proton.me"
        observeInviteRecommendations.emitInvites(
            InviteRecommendations(
                recommendedItems = listOf(RecommendedEmail(email)),
                groupDisplayName = "",
                organizationItems = emptyList()
            )
        )

        viewModel.stateFlow.test {
            assertThat(awaitItem().suggestionsUIState).isInstanceOf(SuggestionsUIState.Content::class.java)

            viewModel.onEmailChange("a")
            // Advance past the debounce timeout (300ms) to trigger recommendations refresh
            advanceTimeBy(301L)

            val loadingEmitted = cancelAndConsumeRemainingEvents()
                .filterIsInstance<Event.Item<SharingWithUIState>>()
                .any { it.value.suggestionsUIState == SuggestionsUIState.Loading }
            assertThat(loadingEmitted).isFalse()
        }
    }

    companion object {
        private const val SHARE_ID = "SharingWithViewModelTest-ShareID"
    }
}
