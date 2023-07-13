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

package proton.android.pass.featureprofile.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import proton.android.pass.appconfig.api.AppConfig
import proton.android.pass.appconfig.api.BuildFlavor
import proton.android.pass.autofill.api.AutofillManager
import proton.android.pass.biometry.BiometryAuthError
import proton.android.pass.biometry.BiometryManager
import proton.android.pass.biometry.BiometryResult
import proton.android.pass.biometry.BiometryStatus
import proton.android.pass.biometry.ContextHolder
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.combineN
import proton.android.pass.common.api.getOrNull
import proton.android.pass.composecomponents.impl.bottombar.AccountType
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.data.api.usecases.ObserveItemCount
import proton.android.pass.data.api.usecases.ObserveMFACount
import proton.android.pass.data.api.usecases.ObserveUpgradeInfo
import proton.android.pass.featureprofile.impl.ProfileSnackbarMessage.AppVersionCopied
import proton.android.pass.featureprofile.impl.ProfileSnackbarMessage.BiometryFailedToAuthenticateError
import proton.android.pass.featureprofile.impl.ProfileSnackbarMessage.BiometryFailedToStartError
import proton.android.pass.featureprofile.impl.ProfileSnackbarMessage.ErrorPerformingOperation
import proton.android.pass.featureprofile.impl.ProfileSnackbarMessage.FingerprintLockDisabled
import proton.android.pass.featureprofile.impl.ProfileSnackbarMessage.FingerprintLockEnabled
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.BiometricLockState
import proton.android.pass.preferences.HasAuthenticated
import proton.android.pass.preferences.UserPreferencesRepository
import proton.pass.domain.PlanType
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val biometryManager: BiometryManager,
    private val autofillManager: AutofillManager,
    private val clipboardManager: ClipboardManager,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val appConfig: AppConfig,
    observeItemCount: ObserveItemCount,
    observeMFACount: ObserveMFACount,
    observeUpgradeInfo: ObserveUpgradeInfo,
) : ViewModel() {

    private val biometricLockState = preferencesRepository
        .getBiometricLockState()
        .distinctUntilChanged()

    private val autofillStatusFlow = autofillManager
        .getAutofillStatus()
        .distinctUntilChanged()

    private val eventFlow: MutableStateFlow<ProfileEvent> = MutableStateFlow(ProfileEvent.Unknown)

    private val upgradeInfoFlow = observeUpgradeInfo().asLoadingResult()

    private val itemSummaryUiStateFlow = combine(
        observeItemCount(itemState = null).asLoadingResult(),
        observeMFACount(),
        upgradeInfoFlow
    ) { itemCountResult, mfaCount, upgradeInfoResult ->
        val itemCount = itemCountResult.getOrNull()
        val upgradeInfo = upgradeInfoResult.getOrNull()
        val isUpgradeAvailable = upgradeInfo?.isUpgradeAvailable ?: false

        val aliasLimit = if (isUpgradeAvailable) {
            upgradeInfo?.plan?.aliasLimit?.limitOrNull()
        } else null

        val mfaLimit = if (isUpgradeAvailable) {
            upgradeInfo?.plan?.totpLimit?.limitOrNull()
        } else null

        ItemSummaryUiState(
            loginCount = itemCount?.login?.toInt() ?: 0,
            notesCount = itemCount?.note?.toInt() ?: 0,
            aliasCount = itemCount?.alias?.toInt() ?: 0,
            creditCardsCount = itemCount?.creditCard?.toInt() ?: 0,
            mfaCount = mfaCount,
            aliasLimit = aliasLimit,
            mfaLimit = mfaLimit
        )
    }

    val state: StateFlow<ProfileUiState> = combineN(
        biometricLockState,
        flowOf(biometryManager.getBiometryStatus()),
        autofillStatusFlow,
        itemSummaryUiStateFlow,
        upgradeInfoFlow,
        eventFlow,
    ) { biometricLock, biometryStatus, autofillStatus, itemSummaryUiState, upgradeInfo, event ->
        val fingerprintSection = when (biometryStatus) {
            BiometryStatus.NotEnrolled -> FingerprintSectionState.NoFingerprintRegistered
            BiometryStatus.NotAvailable -> FingerprintSectionState.NotAvailable
            BiometryStatus.CanAuthenticate -> {
                val available = when (biometricLock) {
                    BiometricLockState.Enabled -> IsButtonEnabled.Enabled
                    BiometricLockState.Disabled -> IsButtonEnabled.Disabled
                }
                FingerprintSectionState.Available(available)
            }
        }

        val (accountType, showUpgradeButton) = when (upgradeInfo) {
            LoadingResult.Loading -> PlanInfo.Hide to false
            is LoadingResult.Error -> {
                PassLogger.w(TAG, upgradeInfo.exception, "Error getting upgradeInfo")
                PlanInfo.Hide to false
            }
            is LoadingResult.Success -> {
                val info = upgradeInfo.data
                when (val plan = info.plan.planType) {
                    PlanType.Free -> PlanInfo.Hide to info.isUpgradeAvailable
                    is PlanType.Paid -> PlanInfo.Unlimited(
                        planName = plan.humanReadable,
                        accountType = AccountType.Unlimited
                    ) to false

                    is PlanType.Trial -> PlanInfo.Trial to info.isUpgradeAvailable
                    is PlanType.Unknown -> PlanInfo.Hide to info.isUpgradeAvailable
                }
            }
        }

        ProfileUiState(
            fingerprintSection = fingerprintSection,
            autofillStatus = autofillStatus,
            itemSummaryUiState = itemSummaryUiState,
            appVersion = appConfig.versionName,
            accountType = accountType,
            event = event,
            showUpgradeButton = showUpgradeButton
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = ProfileUiState.getInitialState(appVersion = appConfig.versionName)
    )

    fun onFingerprintToggle(contextHolder: ContextHolder, value: Boolean) =
        viewModelScope.launch {
            biometryManager.launch(contextHolder)
                .map { result ->
                    when (result) {
                        BiometryResult.Success -> {
                            preferencesRepository.setHasAuthenticated(HasAuthenticated.Authenticated)
                                .onFailure {
                                    val message = "Could not save HasAuthenticated preference"
                                    PassLogger.e(TAG, it, message)
                                }
                            val (lockState, message) = when (!value) {
                                true -> BiometricLockState.Enabled to FingerprintLockEnabled
                                false -> BiometricLockState.Disabled to FingerprintLockDisabled
                            }

                            PassLogger.d(TAG, "Changing BiometricLock to $lockState")
                            preferencesRepository.setBiometricLockState(lockState)
                                .onSuccess { snackbarDispatcher(message) }
                                .onFailure {
                                    PassLogger.e(TAG, it, "Error setting BiometricLockState")
                                    snackbarDispatcher(ErrorPerformingOperation)
                                }
                        }

                        is BiometryResult.Error -> {
                            when (result.cause) {
                                // If the user has cancelled it, do nothing
                                BiometryAuthError.Canceled -> {}
                                BiometryAuthError.UserCanceled -> {}
                                else -> snackbarDispatcher(BiometryFailedToAuthenticateError)
                            }
                        }

                        // User can retry
                        BiometryResult.Failed -> {}
                        is BiometryResult.FailedToStart ->
                            snackbarDispatcher(BiometryFailedToStartError)
                    }
                    PassLogger.i(TAG, "Biometry result: $result")
                }
                .collect { }
        }

    fun onToggleAutofill(value: Boolean) {
        if (!value) {
            autofillManager.openAutofillSelector()
        } else {
            autofillManager.disableAutofill()
        }
    }

    fun copyAppVersion(appVersion: String) = viewModelScope.launch {
        clipboardManager.copyToClipboard(appVersion)
        snackbarDispatcher(AppVersionCopied)
    }

    fun onAppVersionLongClick() = viewModelScope.launch {
        if (appConfig.flavor is BuildFlavor.Alpha) {
            eventFlow.emit(ProfileEvent.OpenFeatureFlags)
        }
    }

    fun clearEvent() = viewModelScope.launch {
        eventFlow.emit(ProfileEvent.Unknown)
    }

    companion object {
        private const val TAG = "ProfileViewModel"
    }
}
