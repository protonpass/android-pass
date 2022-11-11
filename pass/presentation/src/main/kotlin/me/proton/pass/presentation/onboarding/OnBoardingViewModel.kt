package me.proton.pass.presentation.onboarding

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.android.pass.biometry.BiometryAuthError
import me.proton.android.pass.biometry.BiometryManager
import me.proton.android.pass.biometry.BiometryResult
import me.proton.android.pass.biometry.ContextHolder
import me.proton.android.pass.log.PassLogger
import me.proton.android.pass.notifications.api.SnackbarMessageRepository
import me.proton.android.pass.preferences.BiometricLockState
import me.proton.android.pass.preferences.HasAuthenticated
import me.proton.android.pass.preferences.HasCompletedOnBoarding
import me.proton.android.pass.preferences.PreferenceRepository
import me.proton.pass.common.api.asResultWithoutLoading
import me.proton.pass.common.api.logError
import me.proton.pass.common.api.onError
import me.proton.pass.common.api.onSuccess
import me.proton.pass.domain.autofill.AutofillManager
import me.proton.pass.domain.autofill.AutofillStatus
import me.proton.pass.domain.autofill.AutofillSupportedStatus
import me.proton.pass.presentation.onboarding.OnBoardingPageName.Autofill
import me.proton.pass.presentation.onboarding.OnBoardingPageName.Fingerprint
import me.proton.pass.presentation.onboarding.OnBoardingSnackbarMessage.BiometryFailedToAuthenticateError
import me.proton.pass.presentation.onboarding.OnBoardingSnackbarMessage.BiometryFailedToStartError
import me.proton.pass.presentation.onboarding.OnBoardingSnackbarMessage.ErrorPerformingOperation
import me.proton.pass.presentation.onboarding.OnBoardingSnackbarMessage.FingerprintLockEnabled
import javax.inject.Inject

@HiltViewModel
class OnBoardingViewModel @Inject constructor(
    private val autofillManager: AutofillManager,
    private val biometryManager: BiometryManager,
    private val preferenceRepository: PreferenceRepository,
    private val snackbarMessageRepository: SnackbarMessageRepository
) : ViewModel() {

    private val _onBoardingUiState = MutableStateFlow(OnBoardingUiState.Initial)
    val onBoardingUiState: StateFlow<OnBoardingUiState> = _onBoardingUiState

    init {
        viewModelScope.launch {
            val autofillStatus =
                autofillManager.getAutofillStatus().firstOrNull()
            if (shouldHideAutofill(autofillStatus)) {
                _onBoardingUiState.update { it.copy(pageCount = 1) }
            }
        }
    }

    private fun shouldHideAutofill(autofillStatus: AutofillSupportedStatus?): Boolean =
        when (autofillStatus) {
            is AutofillSupportedStatus.Supported -> autofillStatus.status == AutofillStatus.EnabledByOurService
            AutofillSupportedStatus.Unsupported -> true
            else -> true
        }

    fun onMainButtonClick(page: OnBoardingPageName, contextHolder: ContextHolder) {
        when (page) {
            Autofill -> onEnableAutofill()
            Fingerprint -> onEnableFingerprint(contextHolder)
        }
    }

    fun onSkipButtonClick(page: OnBoardingPageName) {
        when (page) {
            Autofill -> onSkipAutofill()
            Fingerprint -> onSkipFingerprint()
        }
    }

    fun onSelectedPageChanged(page: Int) {
        _onBoardingUiState.update { it.copy(selectedPage = page) }
    }

    private fun onEnableAutofill() {
        autofillManager.openAutofillSelector()
        _onBoardingUiState.update { it.copy(selectedPage = 1) }
    }

    private fun onEnableFingerprint(contextHolder: ContextHolder) {
        viewModelScope.launch {
            biometryManager.launch(contextHolder)
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
        snackbarMessageRepository
            .emitSnackbarMessage(BiometryFailedToStartError)
    }

    private suspend fun onBiometryError(result: BiometryResult.Error) {
        when (result.cause) {
            // If the user has cancelled it, do nothing
            BiometryAuthError.Canceled -> {}
            BiometryAuthError.UserCanceled -> {}

            else ->
                snackbarMessageRepository
                    .emitSnackbarMessage(BiometryFailedToAuthenticateError)
        }
    }

    private fun onBiometrySuccess() {
        viewModelScope.launch {
            preferenceRepository.setHasAuthenticated(HasAuthenticated.Authenticated)
                .asResultWithoutLoading()
                .collect { pResult ->
                    pResult.logError(
                        PassLogger,
                        TAG,
                        "Could not save HasAuthenticated preference"
                    )
                }

            PassLogger.d(TAG, "Changing BiometricLock to ${BiometricLockState.Enabled}")
            preferenceRepository.setBiometricLockState(BiometricLockState.Enabled)
                .asResultWithoutLoading()
                .collect { pResult ->
                    pResult
                        .onSuccess {
                            snackbarMessageRepository.emitSnackbarMessage(FingerprintLockEnabled)
                        }
                        .logError(PassLogger, TAG, "Error setting BiometricLockState")
                        .onError {
                            snackbarMessageRepository.emitSnackbarMessage(ErrorPerformingOperation)
                        }
                }

        }
    }

    private fun onSkipAutofill() {
        _onBoardingUiState.update { it.copy(selectedPage = 1) }
    }

    private fun onSkipFingerprint() {
        viewModelScope.launch {
            preferenceRepository.setHasCompletedOnBoarding(HasCompletedOnBoarding.Completed)
                .asResultWithoutLoading()
                .collect { result ->
                    result
                        .onSuccess {
                            _onBoardingUiState.update { it.copy(isCompleted = true) }
                        }
                        .logError(
                            PassLogger,
                            TAG,
                            "Could not save HasCompletedOnBoarding preference"
                        )
                }
        }
    }

    companion object {
        private const val TAG = "OnBoardingViewModel"
    }
}

@Stable
enum class OnBoardingPageName {
    Autofill, Fingerprint
}

