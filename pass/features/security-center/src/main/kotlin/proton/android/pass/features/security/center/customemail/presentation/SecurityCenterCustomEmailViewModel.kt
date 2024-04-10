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

package proton.android.pass.features.security.center.customemail.presentation

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
import proton.android.pass.data.api.usecases.breach.AddBreachCustomEmail
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class SecurityCenterCustomEmailViewModel @Inject constructor(
    private val addBreachCustomEmail: AddBreachCustomEmail,
    private val snackbarDispatcher: SnackbarDispatcher,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    @OptIn(SavedStateHandleSaveableApi::class)
    var emailAddress: String by savedStateHandleProvider.get()
        .saveable { mutableStateOf("") }

    private val isLoadingStateFlow = MutableStateFlow(false)
    private val emailNotValidStateFlow = MutableStateFlow(false)
    private val eventFlow =
        MutableStateFlow<SecurityCenterCustomEmailEvent>(SecurityCenterCustomEmailEvent.Idle)

    internal val state: StateFlow<SecurityCenterCustomEmailState> = combine(
        eventFlow,
        isLoadingStateFlow,
        emailNotValidStateFlow,
        ::SecurityCenterCustomEmailState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SecurityCenterCustomEmailState.Initial
    )

    internal fun onEmailChange(text: String) {
        emailAddress = text
        emailNotValidStateFlow.value = false
    }

    internal fun addCustomEmail() {
        if (!CommonRegex.EMAIL_VALIDATION_REGEX.matches(emailAddress)) {
            emailNotValidStateFlow.value = true
            return
        }
        viewModelScope.launch {
            isLoadingStateFlow.update { true }
            runCatching { addBreachCustomEmail(email = emailAddress) }
                .onSuccess { breachCustomEmail ->
                    if (breachCustomEmail.verified) {
                        PassLogger.i(TAG, "Email already verified")
                        snackbarDispatcher(SecurityCenterCustomEmailSnackbarMessage.EmailAlreadyVerified)
                    } else {
                        eventFlow.update {
                            SecurityCenterCustomEmailEvent.OnEmailSent(
                                breachCustomEmail.id.id,
                                breachCustomEmail.email
                            )
                        }
                    }
                }
                .onFailure {
                    PassLogger.i(TAG, "Failed to add custom email")
                    PassLogger.w(TAG, it)
                    snackbarDispatcher(SecurityCenterCustomEmailSnackbarMessage.ErrorAddingEmail)
                }
            isLoadingStateFlow.update { false }
        }
    }

    internal fun onEventConsumed(event: SecurityCenterCustomEmailEvent) {
        eventFlow.compareAndSet(event, SecurityCenterCustomEmailEvent.Idle)
    }

    companion object {
        private const val TAG = "SecurityCenterCustomEmailViewModel"
    }
}
