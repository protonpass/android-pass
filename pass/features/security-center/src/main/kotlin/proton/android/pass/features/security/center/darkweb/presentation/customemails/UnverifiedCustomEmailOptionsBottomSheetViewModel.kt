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

package proton.android.pass.features.security.center.darkweb.presentation.customemails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.data.api.usecases.breach.RemoveCustomEmail
import proton.android.pass.data.api.usecases.breach.ResendVerificationCode
import proton.android.pass.domain.breach.BreachEmailId
import proton.android.pass.domain.breach.BreachId
import proton.android.pass.features.security.center.darkweb.navigation.CustomEmailNavArgId
import proton.android.pass.features.security.center.darkweb.presentation.customemails.UnverifiedCustomEmailSnackbarMessage.RemoveCustomEmailError
import proton.android.pass.features.security.center.darkweb.presentation.customemails.UnverifiedCustomEmailSnackbarMessage.ResendCodeError
import proton.android.pass.features.security.center.shared.navigation.BreachIdArgId
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.NavParamEncoder
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
internal class UnverifiedCustomEmailOptionsBottomSheetViewModel @Inject constructor(
    private val resendVerificationCode: ResendVerificationCode,
    private val removeCustomEmail: RemoveCustomEmail,
    private val snackbarDispatcher: SnackbarDispatcher,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    private val breachCustomEmailId: BreachEmailId.Custom = savedStateHandleProvider.get()
        .require<String>(BreachIdArgId.key)
        .let { BreachEmailId.Custom(BreachId(it)) }

    private val customEmail: String = savedStateHandleProvider.get()
        .require<String>(CustomEmailNavArgId.key)
        .let { NavParamEncoder.decode(it) }

    private val eventFlow: MutableStateFlow<UnverifiedCustomEmailOptionsEvent> =
        MutableStateFlow(UnverifiedCustomEmailOptionsEvent.Idle)

    private val loadingStateFlow: MutableStateFlow<UnverifiedCustomEmailOptionsLoadingState> =
        MutableStateFlow(UnverifiedCustomEmailOptionsLoadingState.Idle)

    val state: StateFlow<UnverifiedCustomEmailState> = combine(
        eventFlow,
        loadingStateFlow,
        ::UnverifiedCustomEmailState
    )
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = UnverifiedCustomEmailState.Initial
        )

    fun onVerify() = viewModelScope.launch {
        loadingStateFlow.update { UnverifiedCustomEmailOptionsLoadingState.Verify }

        runCatching { resendVerificationCode(id = breachCustomEmailId) }
            .onSuccess {
                eventFlow.update {
                    UnverifiedCustomEmailOptionsEvent.Verify(
                        breachCustomEmailId,
                        customEmail
                    )
                }
            }
            .onFailure {
                PassLogger.w(TAG, "Failed to resend verification code")
                PassLogger.w(TAG, it)
                snackbarDispatcher(ResendCodeError)
            }

        loadingStateFlow.update { UnverifiedCustomEmailOptionsLoadingState.Idle }
    }

    fun onRemove() = viewModelScope.launch {
        loadingStateFlow.update { UnverifiedCustomEmailOptionsLoadingState.Remove }

        runCatching { removeCustomEmail(id = breachCustomEmailId) }
            .onSuccess {
                eventFlow.update { UnverifiedCustomEmailOptionsEvent.Close }
            }
            .onFailure {
                PassLogger.w(TAG, "Failed to remove custom email")
                PassLogger.w(TAG, it)
                snackbarDispatcher(RemoveCustomEmailError)
            }

        loadingStateFlow.update { UnverifiedCustomEmailOptionsLoadingState.Idle }
    }

    internal fun consumeEvent(event: UnverifiedCustomEmailOptionsEvent) {
        eventFlow.compareAndSet(event, UnverifiedCustomEmailOptionsEvent.Idle)
    }

    companion object {
        private const val TAG = "UnverifiedCustomEmailOptionsBottomSheetViewModel"
    }
}
