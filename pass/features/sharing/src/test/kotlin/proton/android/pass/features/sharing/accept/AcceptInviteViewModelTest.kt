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
import proton.android.pass.data.api.usecases.AcceptInviteStatus
import proton.android.pass.data.fakes.usecases.TestAcceptInvite
import proton.android.pass.data.fakes.usecases.TestObserveInvites
import proton.android.pass.data.fakes.usecases.TestRejectInvite
import proton.android.pass.features.sharing.SharingSnackbarMessage
import proton.android.pass.notifications.fakes.TestSnackbarDispatcher
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.TestPendingInvite
import proton.android.pass.domain.ShareId

class AcceptInviteViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var instance: AcceptInviteViewModel

    private lateinit var observeInvites: TestObserveInvites
    private lateinit var acceptInvite: TestAcceptInvite
    private lateinit var rejectInvite: TestRejectInvite
    private lateinit var snackbarDispatcher: TestSnackbarDispatcher

    @Before
    fun setup() {
        observeInvites = TestObserveInvites().apply {
            emitInvites(listOf(TEST_INVITE))
        }
        acceptInvite = TestAcceptInvite()
        rejectInvite = TestRejectInvite()
        snackbarDispatcher = TestSnackbarDispatcher()
        instance = AcceptInviteViewModel(
            acceptInvite = acceptInvite,
            rejectInvite = rejectInvite,
            snackbarDispatcher = snackbarDispatcher,
            observeInvites = observeInvites
        )
    }

    @Test
    fun `sends right values`() = runTest {
        instance.stateFlow.test {
            assertThat(awaitItem()).isEqualTo(
                AcceptInviteUiState(
                    event = AcceptInviteEvent.Idle,
                    content = AcceptInviteUiContent.Content(
                        invite = TEST_INVITE,
                        buttonsState = AcceptInviteButtonsState(
                            confirmLoading = false,
                            rejectLoading = false,
                            enabled = true,
                            hideReject = false
                        ),
                        progressState = AcceptInviteProgressState.Hide
                    )
                )
            )
        }
    }

    @Test
    fun `accept success sends close event and snackbar message`() = runTest {
        val items = 10
        val res = AcceptInviteStatus.Done(items = items, shareId = ShareId("SHARE_ID"))
        acceptInvite.emitValue(Result.success(res))

        instance.onConfirm(TEST_INVITE)
        instance.stateFlow.test {
            assertThat(awaitItem()).isEqualTo(
                AcceptInviteUiState(
                    event = AcceptInviteEvent.Close,
                    content = AcceptInviteUiContent.Content(
                        invite = TEST_INVITE,
                        buttonsState = AcceptInviteButtonsState(
                            confirmLoading = true,
                            rejectLoading = false,
                            enabled = false,
                            hideReject = true
                        ),
                        progressState = AcceptInviteProgressState.Show(
                            downloaded = items,
                            total = items
                        )
                    )
                )
            )
        }
        val message = snackbarDispatcher.snackbarMessage.first().value()
        assertThat(message).isInstanceOf(SharingSnackbarMessage.InviteAccepted::class.java)
    }

    @Test
    fun `accept error sends snackbar message`() = runTest {
        acceptInvite.emitValue(Result.failure(IllegalStateException("test")))
        instance.onConfirm(TEST_INVITE)
        instance.stateFlow.test {
            assertThat(awaitItem()).isEqualTo(
                AcceptInviteUiState(
                    event = AcceptInviteEvent.Idle,
                    content = AcceptInviteUiContent.Content(
                        invite = TEST_INVITE,
                        buttonsState = AcceptInviteButtonsState(
                            confirmLoading = false,
                            rejectLoading = false,
                            enabled = true,
                            hideReject = false
                        ),
                        progressState = AcceptInviteProgressState.Hide
                    )
                )
            )
        }
        val message = snackbarDispatcher.snackbarMessage.first().value()
        assertThat(message).isInstanceOf(SharingSnackbarMessage.InviteAcceptError::class.java)
    }

    @Test
    fun `reject success sends close event and snackbar message`() = runTest {
        instance.onReject(TEST_INVITE)
        instance.stateFlow.test {
            assertThat(awaitItem()).isEqualTo(
                AcceptInviteUiState(
                    event = AcceptInviteEvent.Close,
                    content = AcceptInviteUiContent.Content(
                        invite = TEST_INVITE,
                        buttonsState = AcceptInviteButtonsState(
                            confirmLoading = false,
                            rejectLoading = false,
                            enabled = false,
                            hideReject = false
                        ),
                        progressState = AcceptInviteProgressState.Hide
                    )
                )
            )
        }
        val message = snackbarDispatcher.snackbarMessage.first().value()
        assertThat(message).isInstanceOf(SharingSnackbarMessage.InviteRejected::class.java)
    }

    @Test
    fun `reject error sends close and snackbar message`() = runTest {
        rejectInvite.setResult(Result.failure(IllegalStateException("test")))
        instance.onReject(TEST_INVITE)
        instance.stateFlow.test {
            assertThat(awaitItem()).isEqualTo(
                AcceptInviteUiState(
                    event = AcceptInviteEvent.Close,
                    content = AcceptInviteUiContent.Content(
                        invite = TEST_INVITE,
                        buttonsState = AcceptInviteButtonsState(
                            confirmLoading = false,
                            rejectLoading = false,
                            enabled = false,
                            hideReject = false
                        ),
                        progressState = AcceptInviteProgressState.Hide
                    )
                )
            )
        }
        val message = snackbarDispatcher.snackbarMessage.first().value()
        assertThat(message).isInstanceOf(SharingSnackbarMessage.InviteAcceptError::class.java)
    }

    companion object {
        private const val INVITE_TOKEN = "AcceptInviteViewModelTest.INVITE_TOKEN"
        private const val INVITE_NAME = "AcceptInviteViewModelTest.INVITE_NAME"
        private val TEST_INVITE = TestPendingInvite.create(
            token = INVITE_TOKEN,
            name = INVITE_NAME
        )
    }

}
