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

package proton.android.pass.features.security.center.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import proton.android.pass.securitycenter.api.ObserveSecurityAnalysis
import proton.android.pass.securitycenter.api.sentinel.DisableSentinel
import proton.android.pass.securitycenter.api.sentinel.ObserveIsSentinelEnabled
import javax.inject.Inject

@HiltViewModel
class SecurityCenterHomeViewModel @Inject constructor(
    observeSecurityAnalysis: ObserveSecurityAnalysis,
    observeIsSentinelEnabled: ObserveIsSentinelEnabled,
    private val disableSentinel: DisableSentinel
) : ViewModel() {

    internal val state: StateFlow<SecurityCenterHomeState> = combine(
        observeSecurityAnalysis(),
        observeIsSentinelEnabled()
    ) { securityAnalysis, isSentinelEnabled ->
        SecurityCenterHomeState(
            insecurePasswordsLoadingResult = securityAnalysis.insecurePasswords,
            reusedPasswordsLoadingResult = securityAnalysis.reusedPasswords,
            missing2faResult = securityAnalysis.missing2fa,
            isSentinelEnabled = isSentinelEnabled
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SecurityCenterHomeState.Initial
    )

    internal fun onDisableSentinel() = viewModelScope.launch {
        runCatching { disableSentinel() }
            .onFailure { error ->
                println("JIBIRI: onDisableSentinel -> $error")
            }
            .onSuccess {
                println("JIBIRI: onDisableSentinel -> SUCCESS!")
            }
    }

}
