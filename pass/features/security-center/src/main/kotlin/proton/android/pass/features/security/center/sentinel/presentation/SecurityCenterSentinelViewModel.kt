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

package proton.android.pass.features.security.center.sentinel.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.securitycenter.api.sentinel.EnableSentinel
import javax.inject.Inject

@HiltViewModel
class SecurityCenterSentinelViewModel @Inject constructor(
    private val enableSentinel: EnableSentinel,
    private val snackbarDispatcher: SnackbarDispatcher
) : ViewModel() {

    private val eventFlow =
        MutableStateFlow<SecurityCenterSentinelEvent>(SecurityCenterSentinelEvent.Idle)

    private val isLoadingFlow: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)

    internal val state: StateFlow<SecurityCenterSentinelState> = combine(
        eventFlow,
        isLoadingFlow,
        ::SecurityCenterSentinelState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = SecurityCenterSentinelState.Initial
    )

    internal fun onEventConsumed(event: SecurityCenterSentinelEvent) {
        eventFlow.compareAndSet(event, SecurityCenterSentinelEvent.Idle)
    }

    internal fun onEnableSentinel() = viewModelScope.launch {
        isLoadingFlow.update { IsLoadingState.Loading }

        runCatching { enableSentinel() }
            .onFailure { error ->
                if (error is CancellationException) {
                    SecurityCenterSentinelSnackbarMessage.EnableSentinelCanceled
                } else {
                    SecurityCenterSentinelSnackbarMessage.EnableSentinelError
                }.also { snackbarMessage -> snackbarDispatcher(snackbarMessage) }

                eventFlow.update { SecurityCenterSentinelEvent.OnSentinelEnableError }
            }
            .onSuccess {
                eventFlow.update { SecurityCenterSentinelEvent.OnSentinelEnableSuccess }
            }

        isLoadingFlow.update { IsLoadingState.NotLoading }
    }

    internal fun onLearnMore() {
        eventFlow.update { SecurityCenterSentinelEvent.OnLearnMore }
    }

}
