/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.appconfig.api.AppConfig
import proton.android.pass.appconfig.api.BuildFlavor.Companion.isQuest
import proton.android.pass.autofill.api.AutofillManager
import proton.android.pass.autofill.api.AutofillStatus
import proton.android.pass.autofill.api.AutofillSupportedStatus
import proton.android.pass.biometry.BiometryAuthError
import proton.android.pass.biometry.BiometryManager
import proton.android.pass.biometry.BiometryResult
import proton.android.pass.biometry.BiometryStatus
import proton.android.pass.biometry.BiometryType
import proton.android.pass.biometry.StoreAuthSuccessful
import proton.android.pass.biometry.UnlockMethod
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.commonui.api.ClassHolder
import proton.android.pass.data.api.usecases.ObserveUserAccessData
import proton.android.pass.features.onboarding.OnBoardingPageName.Autofill
import proton.android.pass.features.onboarding.OnBoardingPageName.Fingerprint
import proton.android.pass.features.onboarding.OnBoardingPageName.InvitePending
import proton.android.pass.features.onboarding.OnBoardingPageName.Last
import proton.android.pass.features.onboarding.OnBoardingSnackbarMessage.BiometryFailedToAuthenticateError
import proton.android.pass.features.onboarding.OnBoardingSnackbarMessage.BiometryFailedToStartError
import proton.android.pass.features.onboarding.OnBoardingSnackbarMessage.FingerprintLockEnabled
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.AppLockState
import proton.android.pass.preferences.AppLockTypePreference
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton.android.pass.preferences.HasCompletedOnBoarding
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class OnBoardingViewModel @Inject constructor(
    private val autofillManager: AutofillManager,
    private val biometryManager: BiometryManager,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val observeUserAccessData: ObserveUserAccessData,
    private val storeAuthSuccessful: StoreAuthSuccessful,
    appConfig: AppConfig,
    featureFlagsPreferencesRepository: FeatureFlagsPreferencesRepository
) : ViewModel() {

    private val isQuest = appConfig.flavor.isQuest()
    private val _onBoardingUiState = MutableStateFlow(OnBoardingUiState.Initial)
    val onBoardingUiState: StateFlow<OnBoardingUiState> = _onBoardingUiState

    init {
        viewModelScope.launch {
            val isOnBoardingV2Enable = featureFlagsPreferencesRepository
                .get<Boolean>(FeatureFlag.PASS_MOBILE_ON_BOARDING_V2)
                .firstOrNull()
                ?: false

            _onBoardingUiState.update {
                it.copy(
                    isOnBoardingV2Enable = isOnBoardingV2Enable
                )
            }

            initOnBoarding()
        }
    }

    private fun initOnBoarding() {
        viewModelScope.launch {
            val showInvitePendingAcceptance = async { shouldShowInvitePendingAcceptance() }
            val autofillStatus = async { autofillManager.getAutofillStatus().firstOrNull() }
            val biometryStatus = async {
                if (isQuest) BiometryStatus.NotAvailable else biometryManager.getBiometryStatus()
            }
            val supportedPages = mutableListOf<OnBoardingPageName>()
            if (showInvitePendingAcceptance.await()) {
                supportedPages.add(InvitePending)
            }
            if (shouldShowAutofill(autofillStatus.await())) {
                supportedPages.add(Autofill)
            }
            if (shouldShowFingerprint(biometryStatus.await())) {
                supportedPages.add(Fingerprint)
            }
            supportedPages.add(Last)
            _onBoardingUiState.update { it.copy(enabledPages = supportedPages.toPersistentList()) }
        }
    }

    private suspend fun shouldShowInvitePendingAcceptance(): Boolean = safeRunCatching {
        observeUserAccessData().first()
    }.fold(
        onSuccess = { it?.let { it.waitingNewUserInvites > 0 } ?: false },
        onFailure = {
            PassLogger.w(TAG, "Error getting user access data")
            PassLogger.w(TAG, it)
            false
        }
    )

    private fun shouldShowAutofill(autofillStatus: AutofillSupportedStatus?): Boolean = when (autofillStatus) {
        is AutofillSupportedStatus.Supported -> autofillStatus.status != AutofillStatus.EnabledByOurService
        AutofillSupportedStatus.Unsupported -> false
        else -> false
    }

    private fun shouldShowFingerprint(biometryStatus: BiometryStatus): Boolean = when (biometryStatus) {
        BiometryStatus.CanAuthenticate -> !isQuest
        BiometryStatus.NotAvailable,
        BiometryStatus.NotEnrolled -> false
    }

    fun onMainButtonClick(page: OnBoardingPageName, contextHolder: ClassHolder<Context>) {
        when (page) {
            Autofill -> onEnableAutofill()
            Fingerprint -> onEnableFingerprint(contextHolder)
            Last -> onFinishOnBoarding()
            InvitePending -> goToNextPage()
        }
    }

    private fun onFinishOnBoarding() {
        viewModelScope.launch {
            saveOnBoardingCompleteFlag()
        }
    }

    fun onSkipButtonClick(page: OnBoardingPageName) {
        when (page) {
            Autofill -> goToNextPage()
            Fingerprint -> goToNextPage()
            Last -> {}
            InvitePending -> {}
        }
    }

    fun onSelectedPageChanged(page: Int) {
        _onBoardingUiState.update { it.copy(selectedPage = page) }
    }

    fun clearEvent() {
        _onBoardingUiState.update { it.copy(event = OnboardingEvent.Unknown) }
    }

    private fun onEnableAutofill() {
        viewModelScope.launch {
            autofillManager.openAutofillSelector()
            delay(DELAY_AFTER_AUTOFILL_CLICK)
            if (_onBoardingUiState.value.enabledPages.count() > 1) {
                _onBoardingUiState.update { it.copy(selectedPage = 1) }
            }
        }
    }

    private fun onEnableFingerprint(contextHolder: ClassHolder<Context>) {
        viewModelScope.launch {
            biometryManager.launch(contextHolder, BiometryType.CONFIGURE)
                .collect { result ->
                    when (result) {
                        BiometryResult.Success -> onBiometrySuccess()
                        is BiometryResult.Error -> onBiometryError(result)
                        // User can retry
                        BiometryResult.Failed -> {}
                        is BiometryResult.FailedToStart -> onBiometryFailedToStart()
                    }
                    PassLogger.i(TAG, "Biometry result: $result")
                }
        }
    }

    private suspend fun onBiometryFailedToStart() {
        snackbarDispatcher(BiometryFailedToStartError)
    }

    private suspend fun onBiometryError(result: BiometryResult.Error) {
        when (result.cause) {
            // If the user has cancelled it, do nothing
            BiometryAuthError.Canceled -> {}
            BiometryAuthError.UserCanceled -> {}

            else -> snackbarDispatcher(BiometryFailedToAuthenticateError)
        }
    }

    private fun onBiometrySuccess() {
        viewModelScope.launch {
            storeAuthSuccessful(UnlockMethod.PinOrBiometrics)
            userPreferencesRepository.setAppLockTypePreference(AppLockTypePreference.Biometrics)
            userPreferencesRepository.setAppLockState(AppLockState.Enabled)
            snackbarDispatcher(FingerprintLockEnabled)
            _onBoardingUiState.update { it.copy(selectedPage = it.selectedPage + 1) }
        }
    }

    private fun goToNextPage() {
        viewModelScope.launch {
            _onBoardingUiState.update { it.copy(selectedPage = it.selectedPage + 1) }
        }
    }

    private fun saveOnBoardingCompleteFlag() {
        userPreferencesRepository.setHasCompletedOnBoarding(HasCompletedOnBoarding.Completed)
            .onSuccess {
                _onBoardingUiState.update { it.copy(event = OnboardingEvent.OnboardingCompleted) }
            }
            .onFailure {
                PassLogger.e(TAG, it, "Could not save HasCompletedOnBoarding preference")
            }
    }

    companion object {
        private const val TAG = "OnBoardingViewModel"
        private val DELAY_AFTER_AUTOFILL_CLICK = 300.milliseconds
    }
}
