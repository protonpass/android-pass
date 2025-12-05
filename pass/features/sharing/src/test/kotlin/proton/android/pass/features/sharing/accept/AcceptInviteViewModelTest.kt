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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.fakes.FakeSavedStateHandleProvider
import proton.android.pass.data.api.errors.CannotCreateMoreVaultsError
import proton.android.pass.data.api.usecases.AcceptInviteStatus
import proton.android.pass.data.fakes.usecases.FakeGetItemById
import proton.android.pass.data.fakes.usecases.FakeAcceptInvite
import proton.android.pass.data.fakes.usecases.FakeRejectInvite
import proton.android.pass.data.fakes.usecases.invites.FakeObserveInvite
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareType
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.features.sharing.SharingSnackbarMessage
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.fakes.FakeSnackbarDispatcher
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.TestItem
import proton.android.pass.test.domain.TestPendingInvite

internal class AcceptInviteViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var savedStateHandleProvider: FakeSavedStateHandleProvider
    private lateinit var observeInvite: FakeObserveInvite
    private lateinit var acceptInvite: FakeAcceptInvite
    private lateinit var rejectInvite: FakeRejectInvite
    private lateinit var getItemById: FakeGetItemById
    private lateinit var snackbarDispatcher: FakeSnackbarDispatcher

    private lateinit var viewModel: AcceptInviteViewModel

    @Before
    fun setup() {
        savedStateHandleProvider = FakeSavedStateHandleProvider().apply {
            get()[CommonNavArgId.InviteToken.key] = INVITE_TOKEN
        }
        observeInvite = FakeObserveInvite()
        acceptInvite = FakeAcceptInvite()
        rejectInvite = FakeRejectInvite()
        getItemById = FakeGetItemById()
        snackbarDispatcher = FakeSnackbarDispatcher()

        viewModel = AcceptInviteViewModel(
            savedStateHandleProvider = savedStateHandleProvider,
            observeInvite = observeInvite,
            acceptInvite = acceptInvite,
            rejectInvite = rejectInvite,
            snackbarDispatcher = snackbarDispatcher,
            getItemById = getItemById
        )
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
        val pendingItemInvite = TestPendingInvite.Item.create()
        val expectedState = AcceptInviteStateMother.Item.create(pendingItemInvite = pendingItemInvite)
        observeInvite.emit(pendingItemInvite.toOption())

        viewModel.stateFlow.test {
            val state = awaitItem()

            assertThat(state).isEqualTo(expectedState)
        }
    }

    @Test
    fun `GIVEN item pending invite WHEN invite rejection fails THEN shows error message`() = runTest {
        val pendingItemInvite = TestPendingInvite.Item.create()
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
        val pendingItemInvite = TestPendingInvite.Item.create()
        val rejectionResult: Result<Unit> = Result.failure(IllegalStateException("test"))
        val expectedState = AcceptInviteStateMother.Item.create(
            pendingItemInvite = pendingItemInvite,
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
        val pendingItemInvite = TestPendingInvite.Item.create()
        val acceptInviteStatus = AcceptInviteStatus.UserInviteDone(
            shareId = ShareId(""),
            itemId = ItemId(""),
            items = 1
        )
        val item = TestItem.create(
            shareId = acceptInviteStatus.shareId,
            itemId = acceptInviteStatus.itemId,
            itemType = ItemType.Note(text = "Test note", customFields = emptyList())
        )
        val acceptationResult: Result<AcceptInviteStatus> = Result.success(acceptInviteStatus)
        val expectedMessage = SharingSnackbarMessage.InviteAccepted
        observeInvite.emit(pendingItemInvite.toOption())
        acceptInvite.emitValue(acceptationResult)
        getItemById.emit(Result.success(item))

        viewModel.onAcceptInvite(shareType = ShareType.Item)

        val message = snackbarDispatcher.snackbarMessage.first().value()
        assertThat(message).isEqualTo(expectedMessage)
    }

    @Test
    fun `GIVEN vault pending invite WHEN invite is shown THEN emits vault invite state`() = runTest {
        val vaultInvite = TestPendingInvite.Vault.create()
        val expectedState = AcceptInviteStateMother.Vault.create(pendingVaultInvite = vaultInvite)

        observeInvite.emit(vaultInvite.toOption())

        viewModel.stateFlow.test {
            val state = awaitItem()

            assertThat(state).isEqualTo(expectedState)
        }
    }

    @Test
    fun `GIVEN vault pending invite WHEN invite rejection fails THEN shows error message`() = runTest {
        val pendingVaultInvite = TestPendingInvite.Vault.create()
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
        val pendingVaultInvite = TestPendingInvite.Vault.create()
        val rejectionResult: Result<Unit> = Result.failure(IllegalStateException("test"))
        val expectedState = AcceptInviteStateMother.Vault.create(
            pendingVaultInvite = pendingVaultInvite,
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
        val pendingItemInvite = TestPendingInvite.Item.create()
        val acceptInviteStatus = AcceptInviteStatus.UserInviteDone(
            shareId = ShareId(""),
            itemId = ItemId(""),
            items = 1
        )
        val item = TestItem.create(
            shareId = acceptInviteStatus.shareId,
            itemId = acceptInviteStatus.itemId,
            itemType = ItemType.Note(text = "Test note", emptyList())
        )
        val acceptationResult: Result<AcceptInviteStatus> = Result.success(acceptInviteStatus)
        val expectedState = AcceptInviteStateMother.Item.create(
            pendingItemInvite = pendingItemInvite,
            event = AcceptInviteEvent.OnItemInviteAcceptSuccess(
                shareId = acceptInviteStatus.shareId,
                itemId = acceptInviteStatus.itemId,
                itemCategory = ItemCategory.Note
            )
        )
        observeInvite.emit(pendingItemInvite.toOption())
        acceptInvite.emitValue(acceptationResult)
        getItemById.emit(Result.success(item))

        viewModel.onAcceptInvite(shareType = ShareType.Item)

        viewModel.stateFlow.test {
            val state = awaitItem()

            assertThat(state).isEqualTo(expectedState)
        }
    }

    @Test
    fun `GIVEN vault pending invite WHEN invite accept fails THEN shows error message`() = runTest {
        val pendingVaultInvite = TestPendingInvite.Vault.create()
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
        val pendingVaultInvite = TestPendingInvite.Vault.create()
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
        val pendingVaultInvite = TestPendingInvite.Vault.create()
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
        val pendingVaultInvite = TestPendingInvite.Vault.create()
        val acceptInviteStatus = AcceptInviteStatus.UserInviteDone(
            shareId = ShareId(""),
            itemId = ItemId(""),
            items = 1
        )
        val acceptationResult: Result<AcceptInviteStatus> = Result.success(acceptInviteStatus)
        val expectedState = AcceptInviteStateMother.Vault.create(
            pendingVaultInvite = pendingVaultInvite,
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

    private companion object {

        private const val INVITE_TOKEN = "AcceptInviteViewModelTest.INVITE_TOKEN"

    }

}
