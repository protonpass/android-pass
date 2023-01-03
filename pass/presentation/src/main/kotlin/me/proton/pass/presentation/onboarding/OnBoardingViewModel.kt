package me.proton.pass.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.android.pass.autofill.api.AutofillManager
import me.proton.android.pass.autofill.api.AutofillStatus
import me.proton.android.pass.autofill.api.AutofillSupportedStatus
import me.proton.android.pass.biometry.BiometryAuthError
import me.proton.android.pass.biometry.BiometryManager
import me.proton.android.pass.biometry.BiometryResult
import me.proton.android.pass.biometry.BiometryStatus
import me.proton.android.pass.biometry.ContextHolder
import me.proton.android.pass.log.api.PassLogger
import me.proton.android.pass.notifications.api.SnackbarMessageRepository
import me.proton.android.pass.preferences.BiometricLockState
import me.proton.android.pass.preferences.HasAuthenticated
import me.proton.android.pass.preferences.HasCompletedOnBoarding
import me.proton.android.pass.preferences.UserPreferencesRepository
import me.proton.pass.presentation.onboarding.OnBoardingPageName.Autofill
import me.proton.pass.presentation.onboarding.OnBoardingPageName.Fingerprint
import me.proton.pass.presentation.onboarding.OnBoardingPageName.Last
import me.proton.pass.presentation.onboarding.OnBoardingSnackbarMessage.BiometryFailedToAuthenticateError
import me.proton.pass.presentation.onboarding.OnBoardingSnackbarMessage.BiometryFailedToStartError
import me.proton.pass.presentation.onboarding.OnBoardingSnackbarMessage.ErrorPerformingOperation
import me.proton.pass.presentation.onboarding.OnBoardingSnackbarMessage.FingerprintLockEnabled
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class OnBoardingViewModel @Inject constructor(
    private val autofillManager: AutofillManager,
    private val biometryManager: BiometryManager,
    private val preferenceRepository: UserPreferencesRepository,
    private val snackbarMessageRepository: SnackbarMessageRepository
) : ViewModel() {

    private val _onBoardingUiState = MutableStateFlow(OnBoardingUiState.Initial)
    val onBoardingUiState: StateFlow<OnBoardingUiState> = _onBoardingUiState

    init {
        viewModelScope.launch {
            val autofillStatus = async { autofillManager.getAutofillStatus().firstOrNull() }
            val biometryStatus = async { biometryManager.getBiometryStatus() }
            val supportedPages = mutableSetOf<OnBoardingPageName>()
            if (shouldShowAutofill(autofillStatus.await())) {
                supportedPages.add(Autofill)
            }
            if (shouldShowFingerprint(biometryStatus.await())) {
                supportedPages.add(Fingerprint)
            }
            supportedPages.add(Last)
            _onBoardingUiState.update { it.copy(enabledPages = supportedPages) }
        }
    }

    private fun shouldShowAutofill(autofillStatus: AutofillSupportedStatus?): Boolean =
        when (autofillStatus) {
            is AutofillSupportedStatus.Supported -> autofillStatus.status != AutofillStatus.EnabledByOurService
            AutofillSupportedStatus.Unsupported -> false
            else -> false
        }

    private fun shouldShowFingerprint(biometryStatus: BiometryStatus): Boolean =
        when (biometryStatus) {
            BiometryStatus.CanAuthenticate -> true
            BiometryStatus.NotAvailable,
            BiometryStatus.NotEnrolled -> false
        }

    fun onMainButtonClick(page: OnBoardingPageName, contextHolder: ContextHolder) {
        when (page) {
            Autofill -> onEnableAutofill()
            Fingerprint -> onEnableFingerprint(contextHolder)
            Last -> onFinishOnBoarding()
        }
    }

    private fun onFinishOnBoarding() {
        viewModelScope.launch {
            saveOnBoardingCompleteFlag()
        }
    }

    fun onSkipButtonClick(page: OnBoardingPageName) {
        when (page) {
            Autofill -> onSkipAutofill()
            Fingerprint -> onSkipFingerprint()
            Last -> {}
        }
    }

    fun onSelectedPageChanged(page: Int) {
        _onBoardingUiState.update { it.copy(selectedPage = page) }
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
            saveHasAuthenticatedFlag()
            saveBiometricLockStateFlag()
            _onBoardingUiState.update { it.copy(selectedPage = it.selectedPage + 1) }
        }
    }

    private fun onSkipAutofill() {
        viewModelScope.launch {
            _onBoardingUiState.update { it.copy(selectedPage = it.selectedPage + 1) }
        }
    }

    private fun onSkipFingerprint() {
        viewModelScope.launch {
            _onBoardingUiState.update { it.copy(selectedPage = it.selectedPage + 1) }
        }
    }

    private suspend fun saveHasAuthenticatedFlag() {
        preferenceRepository.setHasAuthenticated(HasAuthenticated.Authenticated)
    }

    private suspend fun saveBiometricLockStateFlag() {
        PassLogger.d(TAG, "Changing BiometricLock to ${BiometricLockState.Enabled}")
        preferenceRepository.setBiometricLockState(BiometricLockState.Enabled)
            .onSuccess {
                snackbarMessageRepository.emitSnackbarMessage(FingerprintLockEnabled)
            }
            .onFailure {
                PassLogger.e(TAG, it, "Error setting BiometricLockState")
                snackbarMessageRepository.emitSnackbarMessage(ErrorPerformingOperation)
            }
    }

    private suspend fun saveOnBoardingCompleteFlag() {
        preferenceRepository.setHasCompletedOnBoarding(HasCompletedOnBoarding.Completed)
            .onSuccess {
                _onBoardingUiState.update { it.copy(isCompleted = true) }
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
