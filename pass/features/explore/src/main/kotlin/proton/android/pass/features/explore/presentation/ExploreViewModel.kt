/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.features.explore.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.ObserveLoginTotpEntries
import proton.android.pass.features.explore.navigation.ExploreNavDestination
import proton.android.pass.features.explore.telemetry.PassExploreBusinessBannerClick
import proton.android.pass.features.explore.telemetry.PassExploreDisplayHome
import proton.android.pass.features.explore.telemetry.PassExploreProductClick
import proton.android.pass.features.explore.telemetry.PassExploreToolClick
import proton.android.pass.protonapps.api.ProtonAppType
import proton.android.pass.protonapps.api.usecases.ObserveProtonApps
import proton.android.pass.protonapps.api.usecases.OpenProtonApp
import proton.android.pass.protonapps.api.usecases.OpenResult
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val observeProtonApps: ObserveProtonApps,
    getUserPlan: GetUserPlan,
    observeLoginTotpEntries: ObserveLoginTotpEntries,
    private val openProtonApp: OpenProtonApp,
    private val telemetryManager: TelemetryManager
) : ViewModel() {

    init {
        telemetryManager.sendEvent(PassExploreDisplayHome)
    }

    val uiState: StateFlow<ExploreUiState> = combine(
        observeProtonApps(),
        getUserPlan(),
        observeLoginTotpEntries().map { it.isNotEmpty() }
    ) { apps, plan, hasTotp ->
        ExploreUiState(
            isLoading = false,
            protonApps = apps.toImmutableList(),
            isBusinessUser = plan.isBusinessPlan,
            showPasswordHealth = SHOW_PASSWORD_HEALTH,
            showCodes = hasTotp
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(INIT_STATE_MS),
        initialValue = ExploreUiState.Initial
    )

    private val _navEvents = MutableSharedFlow<ExploreNavDestination>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val navEvents: SharedFlow<ExploreNavDestination> = _navEvents.asSharedFlow()

    fun onEvent(event: ExploreUiEvent) {
        when (event) {
            ExploreUiEvent.PasswordGeneratorClicked -> {
                telemetryManager.sendEvent(PassExploreToolClick(PassExploreToolClick.Tool.PasswordGenerator))
                emitNav(ExploreNavDestination.PasswordGenerator)
            }
            ExploreUiEvent.DarkWebMonitorClicked -> {
                telemetryManager.sendEvent(PassExploreToolClick(PassExploreToolClick.Tool.DarkWebMonitor))
                emitNav(ExploreNavDestination.DarkWebMonitor)
            }
            ExploreUiEvent.PasswordHealthClicked -> {
                telemetryManager.sendEvent(PassExploreToolClick(PassExploreToolClick.Tool.PasswordHealth))
                emitNav(ExploreNavDestination.PasswordHealth)
            }
            ExploreUiEvent.CodesClicked -> {
                telemetryManager.sendEvent(PassExploreToolClick(PassExploreToolClick.Tool.Codes))
                emitNav(ExploreNavDestination.Codes)
            }
            ExploreUiEvent.AliasesClicked -> {
                telemetryManager.sendEvent(PassExploreToolClick(PassExploreToolClick.Tool.Aliases))
                emitNav(ExploreNavDestination.Aliases)
            }
            is ExploreUiEvent.ProductClicked -> onProductClicked(event.type)
            ExploreUiEvent.BusinessBannerClicked -> {
                telemetryManager.sendEvent(PassExploreBusinessBannerClick)
                emitNav(ExploreNavDestination.ExternalUrl(PROTON_FOR_BUSINESS_URL))
            }
        }
    }

    private fun onProductClicked(type: ProtonAppType) {
        val app = uiState.value.protonApps.firstOrNull { it.type == type } ?: return
        val result = openProtonApp(app)
        val action = when (result) {
            OpenResult.OpenedApp -> PassExploreProductClick.Action.OpenApp
            OpenResult.OpenedPlayStore -> PassExploreProductClick.Action.OpenStore
            OpenResult.OpenedWebFallback -> PassExploreProductClick.Action.OpenWeb
        }
        telemetryManager.sendEvent(PassExploreProductClick(type, action))
    }

    private fun emitNav(destination: ExploreNavDestination) {
        viewModelScope.launch { _navEvents.emit(destination) }
    }

    companion object {
        const val PROTON_FOR_BUSINESS_URL = "https://proton.me/business/pass"

        // TEMP: Password Health row hidden until the dedicated screen exists. Flip to true to re-enable.
        private const val SHOW_PASSWORD_HEALTH = false

        private const val INIT_STATE_MS = 5_000L
    }
}
