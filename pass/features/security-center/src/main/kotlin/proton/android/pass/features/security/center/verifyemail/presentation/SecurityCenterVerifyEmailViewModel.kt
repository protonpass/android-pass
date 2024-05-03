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

package proton.android.pass.features.security.center.verifyemail.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.CommonRegex
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.errors.CustomEmailDoesNotExistException
import proton.android.pass.data.api.errors.InvalidVerificationCodeException
import proton.android.pass.data.api.usecases.breach.ResendVerificationCode
import proton.android.pass.data.api.usecases.breach.VerifyBreachCustomEmail
import proton.android.pass.domain.breach.CustomEmailId
import proton.android.pass.features.security.center.shared.navigation.BreachIdArgId
import proton.android.pass.features.security.center.shared.navigation.EmailArgId
import proton.android.pass.features.security.center.verifyemail.presentation.SecurityCenterVerifyEmailSnackbarMessage.ResendCodeError
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.NavParamEncoder
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class SecurityCenterVerifyEmailViewModel @Inject constructor(
    private val verifyBreachCustomEmail: VerifyBreachCustomEmail,
    private val resendVerificationCode: ResendVerificationCode,
    private val snackbarDispatcher: SnackbarDispatcher,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    private val id: CustomEmailId = savedStateHandleProvider.get()
        .require<String>(BreachIdArgId.key)
        .let(::CustomEmailId)

    private val email: String = savedStateHandleProvider.get()
        .require<String>(EmailArgId.key)
        .let(NavParamEncoder::decode)

    @OptIn(SavedStateHandleSaveableApi::class)
    var code: String by savedStateHandleProvider.get()
        .saveable { mutableStateOf("") }

    private val eventFlow =
        MutableStateFlow<SecurityCenterVerifyEmailEvent>(SecurityCenterVerifyEmailEvent.Idle)

    private val isLoadingFlow: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)
    private val codeNotValidStateFlow = MutableStateFlow(false)

    internal val state: StateFlow<SecurityCenterVerifyEmailState> = combine(
        eventFlow,
        codeNotValidStateFlow,
        isLoadingFlow
    ) { event, codeNotValid, isLoading ->
        SecurityCenterVerifyEmailState(
            email = email,
            isError = codeNotValid,
            event = event,
            isLoadingState = isLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = SecurityCenterVerifyEmailState.default(email)
    )

    internal fun onEventConsumed(event: SecurityCenterVerifyEmailEvent) {
        eventFlow.compareAndSet(event, SecurityCenterVerifyEmailEvent.Idle)
    }

    internal fun onCodeChange(text: String) {
        code = text.replace(CommonRegex.NON_DIGIT_REGEX, "")
        codeNotValidStateFlow.update { false }
    }

    internal fun verifyCode() {
        viewModelScope.launch {
            if (code.isBlank()) {
                codeNotValidStateFlow.update { true }
            } else {
                isLoadingFlow.update { IsLoadingState.Loading }
                runCatching {
                    verifyBreachCustomEmail(id = id, code = code)
                }.onSuccess {
                    eventFlow.update { SecurityCenterVerifyEmailEvent.EmailVerified }
                }.onFailure {
                    PassLogger.i(TAG, "Failed to verify email")
                    PassLogger.w(TAG, it)

                    when (it) {
                        is CustomEmailDoesNotExistException -> {
                            eventFlow.update { SecurityCenterVerifyEmailEvent.GoBackToHome }
                        }
                        is InvalidVerificationCodeException -> {
                            codeNotValidStateFlow.update { true }
                        }
                        else -> {
                            codeNotValidStateFlow.update { true }
                        }
                    }

                }
                isLoadingFlow.update { IsLoadingState.NotLoading }

            }
        }
    }

    internal fun resendCode() {
        viewModelScope.launch {
            runCatching {
                resendVerificationCode(id = id)
            }.onFailure {
                PassLogger.w(TAG, it)
                PassLogger.i(TAG, "Failed to resend code")
                snackbarDispatcher(ResendCodeError)
            }
        }
    }

    companion object {
        private const val TAG = "SecurityCenterVerifyEmailViewModel"
    }
}
