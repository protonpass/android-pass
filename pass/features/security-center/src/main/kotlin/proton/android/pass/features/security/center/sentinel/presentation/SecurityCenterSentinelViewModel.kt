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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.securitycenter.api.sentinel.EnableSentinel
import javax.inject.Inject

@HiltViewModel
class SecurityCenterSentinelViewModel @Inject constructor(
    private val getUserPlan: GetUserPlan,
    private val enableSentinel: EnableSentinel
) : ViewModel() {

    private val eventFlow =
        MutableStateFlow<SecurityCenterSentinelEvent>(SecurityCenterSentinelEvent.Idle)

    internal val state: StateFlow<SecurityCenterSentinelState> = eventFlow
        .map(::SecurityCenterSentinelState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = SecurityCenterSentinelState.Initial
        )

    internal fun onEventConsumed(event: SecurityCenterSentinelEvent) {
        eventFlow.compareAndSet(event, SecurityCenterSentinelEvent.Idle)
    }

    internal fun onEnableSentinel() = viewModelScope.launch {
        getUserPlan().first()
            .let { userPlan ->
                if (userPlan.isPaidPlan) {
                    runCatching { enableSentinel() }
                        .onFailure { error ->
                            println("JIBIRI: onEnableSentinel -> $error")
                        }
                        .onSuccess {
                            println("JIBIRI: onEnableSentinel -> SUCCESS!")
                        }
                } else {
                    eventFlow.update { SecurityCenterSentinelEvent.OnUpsell }
                }
            }
    }

    internal fun onLearnMore() {
        eventFlow.update { SecurityCenterSentinelEvent.OnLearnMore }
    }

}
