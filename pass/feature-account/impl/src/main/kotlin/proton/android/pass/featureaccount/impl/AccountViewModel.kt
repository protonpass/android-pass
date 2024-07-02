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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import me.proton.core.auth.domain.feature.IsFido2Enabled
import me.proton.core.usersettings.domain.usecase.ObserveRegisteredSecurityKeys
import kotlinx.coroutines.flow.take
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.onAccountState
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.combineN
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
    hasExtraPassword: HasExtraPassword,
    accountManager: AccountManager,
    isFido2Enabled: IsFido2Enabled,
    observeRegisteredSecurityKeys: ObserveRegisteredSecurityKeys
) : ViewModel() {

    internal val hasBeenSignedOut: StateFlow<Boolean> =
        accountManager.onAccountState(
            AccountState.Disabled,
            AccountState.Removed,
            initialState = false
        )
            .map { true }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = false
            )

    private val currentUserFlow = observeCurrentUser().take(1)

    private val upgradeInfoFlow = currentUserFlow
        .flatMapLatest { user -> observeUpgradeInfo(userId = user.userId, forceRefresh = true) }
        .distinctUntilChanged()

    val state: StateFlow<AccountUiState> = combineN(
        currentUserFlow.asLoadingResult(),
        upgradeInfoFlow.asLoadingResult(),
        observeCurrentUserSettings().asLoadingResult(),
        oneShot { hasExtraPassword() }.asLoadingResult(),
        featureFlagsPreferencesRepository.get<Boolean>(FeatureFlag.ACCESS_KEY_V1),
        currentUserFlow.flatMapLatest { oneShot { observeRegisteredSecurityKeys(it.userId) } }
    ) { userResult,
        upgradeInfoResult,
        currentUserSettingsResult,
        hasExtraPassword,
        isAccessKeyV1Enabled,
        securityKeys ->

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
                userId = null,
                isFido2Enabled = false,
                registeredSecurityKeys = emptyList()
            )

            is LoadingResult.Success ->
                AccountUiState(
                    email = userResult.data.email,
                    recoveryEmail = currentUserSettingsSuccess?.email?.value,
                    recoveryState = userResult.data.recovery?.state?.enum,
                    plan = plan,
                    isLoadingState = IsLoadingState.NotLoading,
                    showUpgradeButton = isUpgradeAvailable,
                    showSubscriptionButton = isSubscriptionAvailable,
                    showExtraPassword = isAccessKeyV1Enabled,
                    isExtraPasswordEnabled = hasExtraPassword.getOrNull() ?: false,
                    userId = userResult.data.userId,
                    isFido2Enabled = isFido2Enabled(userResult.data.userId),
                    registeredSecurityKeys = securityKeys.firstOrNull() ?: emptyList()
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
