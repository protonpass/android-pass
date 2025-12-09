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

package proton.android.pass.commonpresentation.impl.bars.bottom.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.commonpresentation.api.bars.bottom.home.presentation.HomeBottomBarState
import proton.android.pass.commonpresentation.api.bars.bottom.home.presentation.HomeBottomBarViewModel
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.breach.ObserveHasBreaches
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.monitor.MonitorStatusPreference
import proton.android.pass.securitycenter.api.ObserveSecurityAnalysis
import javax.inject.Inject

@HiltViewModel
class HomeBottomBarViewModelImpl @Inject constructor(
    getUserPlan: GetUserPlan,
    observeHasBreaches: ObserveHasBreaches,
    observeSecurityAnalysis: ObserveSecurityAnalysis,
    userPreferencesRepository: UserPreferencesRepository
) : ViewModel(), HomeBottomBarViewModel {

    private val monitorStatusFlow: Flow<MonitorStatusPreference> = combine(
        observeHasBreaches().asLoadingResult(),
        observeSecurityAnalysis()
    ) { hasBreachesResult, securityAnalysis ->
        when {
            hasBreachesResult is LoadingResult.Success &&
                hasBreachesResult.data -> MonitorStatusPreference.BreachIssues

            securityAnalysis.hasSecurityIssues -> MonitorStatusPreference.VulnerabilityIssues
            hasBreachesResult is LoadingResult.Success -> MonitorStatusPreference.NoIssues
            else -> userPreferencesRepository.observeMonitorStatusPreference().first()
        }.also(userPreferencesRepository::setMonitorStatusPreference)
    }.onStart {
        userPreferencesRepository.observeMonitorStatusPreference()
            .first()
            .also { monitorStatusPreference -> emit(monitorStatusPreference) }
    }.distinctUntilChanged()

    override val state: StateFlow<HomeBottomBarState> = combine(
        getUserPlan(),
        monitorStatusFlow
    ) { plan, monitorStatus ->
        HomeBottomBarState(
            planType = plan.planType,
            monitorStatus = monitorStatus
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = HomeBottomBarState.Initial
    )

}
