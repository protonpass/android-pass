/*
 * Copyright (c) 2023-2026 Proton AG
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

package proton.android.pass.features.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.getOrNull
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.IsSecurityKeysEnabled
import proton.android.pass.data.api.usecases.ObserveAccountStateChanges
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.ObserveCurrentUserSettings
import proton.android.pass.data.api.usecases.ObserveSecurityKeys
import proton.android.pass.data.api.usecases.ObserveUpgradeInfo
import proton.android.pass.data.api.usecases.extrapassword.HasExtraPassword
import proton.android.pass.domain.account.AccountState
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUser,
    observeUpgradeInfo: ObserveUpgradeInfo,
    observeCurrentUserSettings: ObserveCurrentUserSettings,
    hasExtraPassword: HasExtraPassword,
    observeAccountStateChanges: ObserveAccountStateChanges,
    isSecurityKeysEnabled: IsSecurityKeysEnabled,
    observeSecurityKeys: ObserveSecurityKeys
) : ViewModel() {

    internal val hasBeenSignedOut: StateFlow<Boolean> =
        observeAccountStateChanges()
            .filter { it.state == AccountState.Disabled || it.state == AccountState.Removed }
            .map { true }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = false
            )

    private val currentUserFlow = observeCurrentUser().take(1)

    private val upgradeInfoFlow = currentUserFlow
        .flatMapLatest { user -> observeUpgradeInfo(userId = user.userId) }
        .distinctUntilChanged()

    val state: StateFlow<AccountUiState> = combine(
        currentUserFlow.asLoadingResult(),
        upgradeInfoFlow.asLoadingResult(),
        observeCurrentUserSettings().asLoadingResult(),
        oneShot { hasExtraPassword() }.asLoadingResult(),
        currentUserFlow.flatMapLatest { oneShot { observeSecurityKeys(it.userId) } }
    ) { userResult,
        upgradeInfoResult,
        currentUserSettingsResult,
        hasExtraPassword,
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
                userId = null,
                email = null,
                recoveryEmail = currentUserSettingsSuccess?.recoveryEmail,
                recoveryState = null,
                plan = PlanSection.Hide,
                isLoadingState = IsLoadingState.NotLoading,
                showChangePassword = false,
                showRecoveryEmail = false,
                showSecurityKeys = false,
                showUpgradeButton = isUpgradeAvailable,
                showSubscriptionButton = isSubscriptionAvailable,
                showExtraPasswordButton = false,
                isExtraPasswordEnabled = hasExtraPassword.getOrNull() ?: false,
                registeredSecurityKeys = emptyList()
            )

            is LoadingResult.Success ->
                AccountUiState(
                    email = userResult.data.email,
                    recoveryEmail = currentUserSettingsSuccess?.recoveryEmail,
                    recoveryState = userResult.data.recoveryState,
                    plan = plan,
                    isLoadingState = IsLoadingState.NotLoading,
                    showChangePassword = !userResult.data.isSso,
                    showRecoveryEmail = !userResult.data.isSso,
                    showSecurityKeys = !userResult.data.isSso && isSecurityKeysEnabled(userResult.data.userId),
                    showUpgradeButton = !userResult.data.isSso && isUpgradeAvailable,
                    showSubscriptionButton = isSubscriptionAvailable,
                    showExtraPasswordButton = !userResult.data.isSso,
                    isExtraPasswordEnabled = hasExtraPassword.getOrNull() ?: false,
                    userId = userResult.data.userId,
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
