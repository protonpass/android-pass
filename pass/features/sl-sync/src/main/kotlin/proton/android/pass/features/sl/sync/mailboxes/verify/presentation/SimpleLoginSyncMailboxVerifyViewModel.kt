/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.features.sl.sync.mailboxes.verify.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.errors.InvalidVerificationCodeException
import proton.android.pass.data.api.usecases.simplelogin.ResendSimpleLoginAliasMailboxVerificationCode
import proton.android.pass.data.api.usecases.simplelogin.VerifySimpleLoginAliasMailbox
import proton.android.pass.features.sl.sync.mailboxes.verify.navigation.SimpleLoginSyncMailboxVerifyEmailNavArgId
import proton.android.pass.features.sl.sync.shared.navigation.mailboxes.SimpleLoginSyncMailboxIdNavArgId
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class SimpleLoginSyncMailboxVerifyViewModel @Inject constructor(
    savedStateHandleProvider: SavedStateHandleProvider,
    private val verifyAliasMailbox: VerifySimpleLoginAliasMailbox,
    private val resendAliasMailboxVerificationCode: ResendSimpleLoginAliasMailboxVerificationCode,
    private val snackbarDispatcher: SnackbarDispatcher
) : ViewModel() {

    private val mailboxId = savedStateHandleProvider.get()
        .require<Long>(SimpleLoginSyncMailboxIdNavArgId.key)

    private val mailboxEmailFlow = savedStateHandleProvider.get()
        .getStateFlow(SimpleLoginSyncMailboxVerifyEmailNavArgId.key, "")

    @OptIn(SavedStateHandleSaveableApi::class)
    private var verificationCodeMutableState: String by savedStateHandleProvider.get()
        .saveable { mutableStateOf("") }

    private val verificationCodeTimerRestarterFlow = MutableStateFlow(false)

    private val verificationCodeTimerSecondsFlow = verificationCodeTimerRestarterFlow
        .onStart { emit(true) }
        .filter { shouldStartTimer -> shouldStartTimer }
        .flatMapLatest {
            flow {
                verificationCodeTimerRestarterFlow.update { false }

                var remainingSeconds = INITIAL_VERIFICATION_CODE_TIMER_SECONDS
                emit(remainingSeconds)

                while (currentCoroutineContext().isActive && remainingSeconds > 0) {
                    delay(timeMillis = VERIFICATION_CODE_TIMER_INTERVAL_MILLIS)
                    remainingSeconds--
                    emit(remainingSeconds)
                }
            }
        }

    private val eventFlow = MutableStateFlow<SimpleLoginSyncMailboxVerifyEvent>(
        value = SimpleLoginSyncMailboxVerifyEvent.Idle
    )

    private val isLoadingStateFlow: MutableStateFlow<IsLoadingState> = MutableStateFlow(
        value = IsLoadingState.NotLoading
    )

    internal val verificationCodeState: String
        get() = verificationCodeMutableState

    internal val stateFlow: StateFlow<SimpleLoginSyncMailboxVerifyState> = combine(
        mailboxEmailFlow,
        verificationCodeTimerSecondsFlow,
        eventFlow,
        isLoadingStateFlow,
        ::SimpleLoginSyncMailboxVerifyState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SimpleLoginSyncMailboxVerifyState.Initial
    )

    internal fun onConsumeEvent(event: SimpleLoginSyncMailboxVerifyEvent) {
        eventFlow.compareAndSet(event, SimpleLoginSyncMailboxVerifyEvent.Idle)
    }

    internal fun onVerificationCodeChanged(newVerificationCode: String) {
        verificationCodeMutableState = newVerificationCode
    }

    internal fun onVerifyAliasMailbox() {
        viewModelScope.launch {
            isLoadingStateFlow.update { IsLoadingState.Loading }

            runCatching {
                verifyAliasMailbox(
                    mailboxId = mailboxId,
                    verificationCode = verificationCodeState
                )
            }.onFailure { error ->
                PassLogger.w(TAG, "There was an error verifying alias mailbox")
                PassLogger.w(TAG, error)
                if (error is InvalidVerificationCodeException) {
                    SimpleLoginSyncMailboxVerifySnackbarMessage.VerifyCodeError
                } else {
                    SimpleLoginSyncMailboxVerifySnackbarMessage.VerifyMailboxError
                }.also { snackbarMessage -> snackbarDispatcher(snackbarMessage) }
            }.onSuccess {
                snackbarDispatcher(SimpleLoginSyncMailboxVerifySnackbarMessage.VerifyMailboxSuccess)
                eventFlow.update { SimpleLoginSyncMailboxVerifyEvent.OnVerifyAliasMailboxSuccess }
            }

            isLoadingStateFlow.update { IsLoadingState.NotLoading }
        }
    }

    internal fun onResendVerificationCode() {
        viewModelScope.launch {
            isLoadingStateFlow.update { IsLoadingState.Loading }

            runCatching { resendAliasMailboxVerificationCode(mailboxId = mailboxId) }
                .onFailure { error ->
                    PassLogger.w(TAG, "There was an error resending alias mailbox verification code")
                    PassLogger.w(TAG, error)
                    snackbarDispatcher(SimpleLoginSyncMailboxVerifySnackbarMessage.ResendCodeError)
                }
                .onSuccess {
                    verificationCodeTimerRestarterFlow.update { true }
                    snackbarDispatcher(SimpleLoginSyncMailboxVerifySnackbarMessage.ResendCodeSuccess)
                }

            isLoadingStateFlow.update { IsLoadingState.NotLoading }
        }
    }

    private companion object {

        private const val TAG = "SimpleLoginSyncMailboxVerifyViewModel"

        private const val INITIAL_VERIFICATION_CODE_TIMER_SECONDS = 30

        private const val VERIFICATION_CODE_TIMER_INTERVAL_MILLIS = 1_000L

    }

}
