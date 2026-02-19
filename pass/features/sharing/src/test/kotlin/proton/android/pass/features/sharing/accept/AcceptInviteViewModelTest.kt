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

package proton.android.pass.features.sharing.accept

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.fakes.FakeSavedStateHandleProvider
import proton.android.pass.data.api.errors.CannotCreateMoreVaultsError
import proton.android.pass.data.api.usecases.AcceptInviteStatus
import proton.android.pass.data.fakes.usecases.FakeAcceptInvite
import proton.android.pass.data.fakes.usecases.FakeObserveCurrentUser
import proton.android.pass.data.fakes.usecases.FakeRejectInvite
import proton.android.pass.data.fakes.usecases.invites.FakeObserveInvite
import proton.android.pass.data.fakes.repositories.FakeGroupRepository
import proton.android.pass.domain.GroupId
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareType
import proton.android.pass.features.sharing.SharingSnackbarMessage
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.fakes.FakeSnackbarDispatcher
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.GroupTestFactory
import proton.android.pass.test.domain.ItemTestFactory
import proton.android.pass.test.domain.PendingInviteTestFactory
import proton.android.pass.test.domain.UserTestFactory

internal class AcceptInviteViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var savedStateHandleProvider: FakeSavedStateHandleProvider
    private lateinit var observeInvite: FakeObserveInvite
    private lateinit var observeCurrentUser: FakeObserveCurrentUser
    private lateinit var groupRepository: FakeGroupRepository
    private lateinit var acceptInvite: FakeAcceptInvite
    private lateinit var rejectInvite: FakeRejectInvite
    private lateinit var snackbarDispatcher: FakeSnackbarDispatcher

    private lateinit var viewModel: AcceptInviteViewModel

    @Before
    fun setup() {
        savedStateHandleProvider = FakeSavedStateHandleProvider().apply {
            get()[CommonNavArgId.InviteToken.key] = INVITE_TOKEN
        }
        observeInvite = FakeObserveInvite()
        observeCurrentUser = FakeObserveCurrentUser()
        groupRepository = FakeGroupRepository()
        acceptInvite = FakeAcceptInvite()
        rejectInvite = FakeRejectInvite()
        snackbarDispatcher = FakeSnackbarDispatcher()

        viewModel = AcceptInviteViewModel(
            savedStateHandleProvider = savedStateHandleProvider,
            observeInvite = observeInvite,
            observeCurrentUser = observeCurrentUser,
            groupRepository = groupRepository,
            acceptInvite = acceptInvite,
            rejectInvite = rejectInvite,
            snackbarDispatcher = snackbarDispatcher
        )

        observeCurrentUser.sendUser(UserTestFactory.create(userId = USER_ID))
    }

    @Test
    fun `WHEN view model is created THEN emits initial state`() = runTest {
        val expectedState = AcceptInviteState.Initial

        viewModel.stateFlow.test {
            val state = awaitItem()

            assertThat(state).isEqualTo(expectedState)
        }
    }

    @Test
    fun `GIVEN item pending invite WHEN invite is shown THEN emits item invite state`() = runTest {
        val pendingItemInvite = PendingInviteTestFactory.Item.create()
        val expectedState = AcceptInviteStateTestFactory.Item.create(
            invite = AcceptInviteUiModel.Item.User(inviterEmail = pendingItemInvite.inviterEmail)
        )
        observeInvite.emit(pendingItemInvite.toOption())

        viewModel.stateFlow.test {
            val state = awaitItem()

            assertThat(state).isEqualTo(expectedState)
        }
    }

    @Test
    fun `GIVEN item pending invite WHEN invite rejection fails THEN shows error message`() = runTest {
        val pendingItemInvite = PendingInviteTestFactory.Item.create()
        val rejectionResult: Result<Unit> = Result.failure(IllegalStateException("test"))
        val expectedMessage = SharingSnackbarMessage.InviteRejectError
        observeInvite.emit(pendingItemInvite.toOption())
        rejectInvite.setResult(rejectionResult)

        viewModel.onRejectInvite()

        val message = snackbarDispatcher.snackbarMessage.first().value()
        assertThat(message).isEqualTo(expectedMessage)
    }

    @Test
    fun `GIVEN item pending invite WHEN invite rejection fails THEN emits rejecting state`() = runTest {
        val pendingItemInvite = PendingInviteTestFactory.Item.create()
        val rejectionResult: Result<Unit> = Result.failure(IllegalStateException("test"))
        val expectedState = AcceptInviteStateTestFactory.Item.create(
            invite = AcceptInviteUiModel.Item.User(inviterEmail = pendingItemInvite.inviterEmail),
            progress = AcceptInviteProgress.Rejecting,
            event = AcceptInviteEvent.Close
        )
        observeInvite.emit(pendingItemInvite.toOption())
        rejectInvite.setResult(rejectionResult)

        viewModel.onRejectInvite()

        viewModel.stateFlow.test {
            val state = awaitItem()

            assertThat(state).isEqualTo(expectedState)
        }
    }

    @Test
    fun `GIVEN item pending invite WHEN invite accept is done THEN shows success message`() = runTest {
        val pendingItemInvite = PendingInviteTestFactory.Item.create()
        val acceptInviteStatus = AcceptInviteStatus.UserInviteDone(
            shareId = ShareId(""),
            itemId = ItemId(""),
            items = 1
        )
        val acceptationResult: Result<AcceptInviteStatus> = Result.success(acceptInviteStatus)
        val expectedMessage = SharingSnackbarMessage.InviteAccepted
        observeInvite.emit(pendingItemInvite.toOption())
        acceptInvite.emitValue(acceptationResult)

        viewModel.onAcceptInvite(shareType = ShareType.Item)

        val message = snackbarDispatcher.snackbarMessage.first().value()
        assertThat(message).isEqualTo(expectedMessage)
    }

    @Test
    fun `GIVEN vault pending invite WHEN invite is shown THEN emits vault invite state`() = runTest {
        val vaultInvite = PendingInviteTestFactory.Vault.create()
        val expectedState = AcceptInviteStateTestFactory.Vault.create(
            invite = AcceptInviteUiModel.Vault.User(
                inviterEmail = vaultInvite.inviterEmail,
                name = vaultInvite.name,
                itemCount = vaultInvite.itemCount,
                memberCount = vaultInvite.memberCount,
                icon = vaultInvite.icon,
                color = vaultInvite.color
            )
        )

        observeInvite.emit(vaultInvite.toOption())

        viewModel.stateFlow.test {
            val state = awaitItem()

            assertThat(state).isEqualTo(expectedState)
        }
    }

    @Test
    fun `GIVEN vault pending invite WHEN invite rejection fails THEN shows error message`() = runTest {
        val pendingVaultInvite = PendingInviteTestFactory.Vault.create()
        val rejectionResult: Result<Unit> = Result.failure(IllegalStateException("test"))
        val expectedMessage = SharingSnackbarMessage.InviteRejectError
        observeInvite.emit(pendingVaultInvite.toOption())
        rejectInvite.setResult(rejectionResult)

        viewModel.onRejectInvite()

        val message = snackbarDispatcher.snackbarMessage.first().value()
        assertThat(message).isEqualTo(expectedMessage)
    }

    @Test
    fun `GIVEN vault pending invite WHEN invite rejection fails THEN emits rejecting state`() = runTest {
        val pendingVaultInvite = PendingInviteTestFactory.Vault.create()
        val rejectionResult: Result<Unit> = Result.failure(IllegalStateException("test"))
        val expectedState = AcceptInviteStateTestFactory.Vault.create(
            invite = AcceptInviteUiModel.Vault.User(
                inviterEmail = pendingVaultInvite.inviterEmail,
                name = pendingVaultInvite.name,
                itemCount = pendingVaultInvite.itemCount,
                memberCount = pendingVaultInvite.memberCount,
                icon = pendingVaultInvite.icon,
                color = pendingVaultInvite.color
            ),
            progress = AcceptInviteProgress.Rejecting,
            event = AcceptInviteEvent.Close
        )
        observeInvite.emit(pendingVaultInvite.toOption())
        rejectInvite.setResult(rejectionResult)

        viewModel.onRejectInvite()

        viewModel.stateFlow.test {
            val state = awaitItem()

            assertThat(state).isEqualTo(expectedState)
        }
    }

    @Test
    fun `GIVEN item pending invite WHEN invite accept is done THEN emits done state`() = runTest {
        val pendingItemInvite = PendingInviteTestFactory.Item.create()
        val acceptInviteStatus = AcceptInviteStatus.UserInviteDone(
            shareId = ShareId(""),
            itemId = ItemId(""),
            items = 1
        )
        val item = ItemTestFactory.create(
            shareId = acceptInviteStatus.shareId,
            itemId = acceptInviteStatus.itemId,
            itemType = ItemType.Note(text = "Test note", emptyList())
        )
        val acceptationResult: Result<AcceptInviteStatus> = Result.success(acceptInviteStatus)
        val expectedState = AcceptInviteStateTestFactory.Item.create(
            invite = AcceptInviteUiModel.Item.User(inviterEmail = pendingItemInvite.inviterEmail),
            event = AcceptInviteEvent.OnItemInviteAcceptSuccess(
                shareId = acceptInviteStatus.shareId,
                itemId = acceptInviteStatus.itemId
            )
        )
        observeInvite.emit(pendingItemInvite.toOption())
        acceptInvite.emitValue(acceptationResult)

        viewModel.onAcceptInvite(shareType = ShareType.Item)

        viewModel.stateFlow.test {
            val state = awaitItem()

            assertThat(state).isEqualTo(expectedState)
        }
    }

    @Test
    fun `GIVEN vault pending invite WHEN invite accept fails THEN shows error message`() = runTest {
        val pendingVaultInvite = PendingInviteTestFactory.Vault.create()
        val acceptationResult: Result<AcceptInviteStatus> = Result.failure(IllegalStateException("test"))
        val expectedMessage = SharingSnackbarMessage.InviteAcceptError
        observeInvite.emit(pendingVaultInvite.toOption())
        acceptInvite.emitValue(acceptationResult)

        viewModel.onAcceptInvite(shareType = ShareType.Vault)

        val message = snackbarDispatcher.snackbarMessage.first().value()
        assertThat(message).isEqualTo(expectedMessage)
    }

    @Test
    fun `GIVEN vault pending invite WHEN invite accept reach vault limit THEN shows error message`() = runTest {
        val pendingVaultInvite = PendingInviteTestFactory.Vault.create()
        val acceptationResult: Result<AcceptInviteStatus> = Result.failure(CannotCreateMoreVaultsError())
        val expectedMessage = SharingSnackbarMessage.InviteAcceptErrorCannotCreateMoreVaults
        observeInvite.emit(pendingVaultInvite.toOption())
        acceptInvite.emitValue(acceptationResult)

        viewModel.onAcceptInvite(shareType = ShareType.Vault)

        val message = snackbarDispatcher.snackbarMessage.first().value()
        assertThat(message).isEqualTo(expectedMessage)
    }

    @Test
    fun `GIVEN vault pending invite WHEN invite accept is done THEN shows success message`() = runTest {
        val pendingVaultInvite = PendingInviteTestFactory.Vault.create()
        val acceptInviteStatus = AcceptInviteStatus.UserInviteDone(
            shareId = ShareId(""),
            itemId = ItemId(""),
            items = 1
        )
        val acceptationResult: Result<AcceptInviteStatus> = Result.success(acceptInviteStatus)
        val expectedMessage = SharingSnackbarMessage.InviteAccepted
        observeInvite.emit(pendingVaultInvite.toOption())
        acceptInvite.emitValue(acceptationResult)

        viewModel.onAcceptInvite(shareType = ShareType.Vault)

        val message = snackbarDispatcher.snackbarMessage.first().value()
        assertThat(message).isEqualTo(expectedMessage)
    }

    @Test
    fun `GIVEN vault pending invite WHEN invite accept is done THEN emits done state`() = runTest {
        val pendingVaultInvite = PendingInviteTestFactory.Vault.create()
        val acceptInviteStatus = AcceptInviteStatus.UserInviteDone(
            shareId = ShareId(""),
            itemId = ItemId(""),
            items = 1
        )
        val acceptationResult: Result<AcceptInviteStatus> = Result.success(acceptInviteStatus)
        val expectedState = AcceptInviteStateTestFactory.Vault.create(
            invite = AcceptInviteUiModel.Vault.User(
                inviterEmail = pendingVaultInvite.inviterEmail,
                name = pendingVaultInvite.name,
                itemCount = pendingVaultInvite.itemCount,
                memberCount = pendingVaultInvite.memberCount,
                icon = pendingVaultInvite.icon,
                color = pendingVaultInvite.color
            ),
            event = AcceptInviteEvent.OnVaultInviteAcceptSuccess(
                shareId = acceptInviteStatus.shareId
            )
        )
        observeInvite.emit(pendingVaultInvite.toOption())
        acceptInvite.emitValue(acceptationResult)

        viewModel.onAcceptInvite(shareType = ShareType.Vault)

        viewModel.stateFlow.test {
            val state = awaitItem()

            assertThat(state).isEqualTo(expectedState)
        }
    }

    @Test
    fun `GIVEN group item pending invite WHEN invite is shown THEN emits group ui model with required group name`() =
        runTest {
            val pendingGroupInvite = PendingInviteTestFactory.GroupItem.create()
            groupRepository.groups = listOf(
                GroupTestFactory.create(
                    id = GroupId(pendingGroupInvite.invitedGroupId),
                    email = pendingGroupInvite.invitedEmail
                )
            )
            val expectedState = AcceptInviteStateTestFactory.Item.create(
                invite = AcceptInviteUiModel.Item.Group(
                    inviterEmail = pendingGroupInvite.inviterEmail,
                    groupName = groupRepository.groups.first().name
                )
            )
            observeInvite.emit(pendingGroupInvite.toOption())

            viewModel.stateFlow.test {
                val state = awaitItem()

                assertThat(state).isEqualTo(expectedState)
            }
        }

    @Test
    fun `GIVEN group vault pending invite WHEN group cannot be resolved THEN uses invited email`() = runTest {
        val pendingGroupInvite = PendingInviteTestFactory.GroupVault.create(
            invitedGroupId = "missing-group-id",
            invitedEmail = "group-fallback@email"
        )
        val expectedState = AcceptInviteStateTestFactory.Vault.create(
            invite = AcceptInviteUiModel.Vault.Group(
                inviterEmail = pendingGroupInvite.inviterEmail,
                groupName = pendingGroupInvite.invitedEmail,
                name = pendingGroupInvite.name,
                itemCount = pendingGroupInvite.itemCount,
                memberCount = pendingGroupInvite.memberCount,
                icon = pendingGroupInvite.icon,
                color = pendingGroupInvite.color
            )
        )
        observeInvite.emit(pendingGroupInvite.toOption())

        viewModel.stateFlow.test {
            val state = awaitItem()

            assertThat(state).isEqualTo(expectedState)
        }
    }

    private companion object {

        private const val INVITE_TOKEN = "AcceptInviteViewModelTest.INVITE_TOKEN"
        private val USER_ID = UserId("user-id")

    }

}
