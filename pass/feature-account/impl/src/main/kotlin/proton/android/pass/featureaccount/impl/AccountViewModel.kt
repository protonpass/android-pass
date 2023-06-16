/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.featureaccount.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.ObserveUpgradeInfo
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUser,
    observeUpgradeInfo: ObserveUpgradeInfo
) : ViewModel() {

    private val currentUser = observeCurrentUser()
        .distinctUntilChanged()

    val state: StateFlow<AccountUiState> = combine(
        currentUser.asLoadingResult(),
        observeUpgradeInfo(forceRefresh = true).asLoadingResult()
    ) { userResult, upgradeInfoResult ->
        val plan = when (upgradeInfoResult) {
            is LoadingResult.Error -> {
                PassLogger.e(TAG, upgradeInfoResult.exception, "Error retrieving user plan")
                PlanSection.Hide
            }

            LoadingResult.Loading -> PlanSection.Loading
            is LoadingResult.Success ->
                PlanSection.Data(
                    planName = upgradeInfoResult.data.plan.planType.humanReadableName()
                )
        }
        val showUpgradeButton = when (upgradeInfoResult) {
            is LoadingResult.Error -> false
            LoadingResult.Loading -> false
            is LoadingResult.Success -> upgradeInfoResult.data.isUpgradeAvailable
        }
        when (userResult) {
            LoadingResult.Loading -> AccountUiState.Initial
            is LoadingResult.Error -> AccountUiState(
                email = null,
                plan = PlanSection.Hide,
                isLoadingState = IsLoadingState.NotLoading,
                showUpgradeButton = showUpgradeButton
            )

            is LoadingResult.Success -> AccountUiState(
                email = userResult.data.email,
                plan = plan,
                isLoadingState = IsLoadingState.NotLoading,
                showUpgradeButton = showUpgradeButton
            )
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = AccountUiState.Initial
        )

    companion object {
        private const val TAG = "AccountViewModel"
    }
}
