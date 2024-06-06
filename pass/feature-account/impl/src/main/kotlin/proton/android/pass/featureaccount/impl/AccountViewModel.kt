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
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.getOrNull
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.ObserveCurrentUserSettings
import proton.android.pass.data.api.usecases.ObserveUpgradeInfo
import proton.android.pass.data.api.usecases.extrapassword.HasExtraPassword
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUser,
    observeUpgradeInfo: ObserveUpgradeInfo,
    observeCurrentUserSettings: ObserveCurrentUserSettings,
    featureFlagsPreferencesRepository: FeatureFlagsPreferencesRepository,
    hasExtraPassword: HasExtraPassword
) : ViewModel() {

    private val currentUser = observeCurrentUser()
        .distinctUntilChanged()

    val state: StateFlow<AccountUiState> = combine(
        currentUser.asLoadingResult(),
        observeUpgradeInfo(forceRefresh = true).asLoadingResult(),
        observeCurrentUserSettings().asLoadingResult(),
        oneShot { hasExtraPassword() }.asLoadingResult(),
        featureFlagsPreferencesRepository.get<Boolean>(FeatureFlag.ACCESS_KEY_V1)
    ) { userResult, upgradeInfoResult, currentUserSettingsResult, hasExtraPassword, isAccessKeyV1Enabled ->
        val plan = when (upgradeInfoResult) {
            is LoadingResult.Error -> {
                PassLogger.w(TAG, "Error retrieving user plan")
                PassLogger.w(TAG, upgradeInfoResult.exception)
                PlanSection.Hide
            }

            LoadingResult.Loading -> PlanSection.Loading
            is LoadingResult.Success ->
                PlanSection.Data(
                    planName = upgradeInfoResult.data.plan.planType.humanReadableName
                )
        }

        val upgradeInfoSuccess = upgradeInfoResult.getOrNull()
        val currentUserSettingsSuccess = currentUserSettingsResult.getOrNull()
        val isUpgradeAvailable = upgradeInfoSuccess?.isUpgradeAvailable ?: false
        val isSubscriptionAvailable = upgradeInfoSuccess?.isSubscriptionAvailable ?: false
        when (userResult) {
            LoadingResult.Loading -> AccountUiState.Initial
            is LoadingResult.Error -> AccountUiState(
                email = null,
                recoveryEmail = currentUserSettingsSuccess?.email?.value,
                recoveryState = null,
                plan = PlanSection.Hide,
                isLoadingState = IsLoadingState.NotLoading,
                showUpgradeButton = isUpgradeAvailable,
                showSubscriptionButton = isSubscriptionAvailable,
                showExtraPassword = isAccessKeyV1Enabled,
                isExtraPasswordEnabled = hasExtraPassword.getOrNull() ?: false,
                userId = null
            )

            is LoadingResult.Success -> AccountUiState(
                email = userResult.data.email,
                recoveryEmail = currentUserSettingsSuccess?.email?.value,
                recoveryState = userResult.data.recovery?.state?.enum,
                plan = plan,
                isLoadingState = IsLoadingState.NotLoading,
                showUpgradeButton = isUpgradeAvailable,
                showSubscriptionButton = isSubscriptionAvailable,
                showExtraPassword = isAccessKeyV1Enabled,
                isExtraPasswordEnabled = hasExtraPassword.getOrNull() ?: false,
                userId = userResult.data.userId
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
