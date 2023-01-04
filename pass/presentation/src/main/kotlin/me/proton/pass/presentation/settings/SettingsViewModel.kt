package me.proton.pass.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.android.pass.appconfig.api.AppConfig
import me.proton.android.pass.autofill.api.AutofillManager
import me.proton.android.pass.autofill.api.AutofillSupportedStatus
import me.proton.android.pass.biometry.BiometryAuthError
import me.proton.android.pass.biometry.BiometryManager
import me.proton.android.pass.biometry.BiometryResult
import me.proton.android.pass.biometry.BiometryStatus
import me.proton.android.pass.biometry.ContextHolder
import me.proton.android.pass.clipboard.api.ClipboardManager
import me.proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import me.proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import me.proton.android.pass.data.api.usecases.RefreshContent
import me.proton.android.pass.log.api.PassLogger
import me.proton.android.pass.notifications.api.SnackbarMessageRepository
import me.proton.android.pass.preferences.BiometricLockState
import me.proton.android.pass.preferences.HasAuthenticated
import me.proton.android.pass.preferences.ThemePreference
import me.proton.android.pass.preferences.UserPreferencesRepository
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.pass.common.api.Result
import me.proton.pass.common.api.asResultWithoutLoading
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val preferencesRepository: UserPreferencesRepository,
    private val biometryManager: BiometryManager,
    private val snackbarMessageRepository: SnackbarMessageRepository,
    private val autofillManager: AutofillManager,
    private val refreshContent: RefreshContent,
    private val clipboardManager: ClipboardManager,
    private val appConfig: AppConfig
) : ViewModel() {

    private val biometricLockState: Flow<BiometricLockState> = preferencesRepository
        .getBiometricLockState()
        .asResultWithoutLoading()
        .map { getFingerprintSection(it) }
        .distinctUntilChanged()

    private val themeState: Flow<ThemePreference> = preferencesRepository
        .getThemePreference()
        .asResultWithoutLoading()
        .map { getTheme(it) }
        .distinctUntilChanged()

    private val autofillState: Flow<AutofillSupportedStatus> = autofillManager
        .getAutofillStatus()
        .distinctUntilChanged()

    private val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)

    val state: StateFlow<SettingsUiState> = combine(
        biometricLockState,
        themeState,
        autofillState,
        isLoadingState
    ) { biometricLock, theme, autofill, loading ->
        val fingerprintSection = when (biometryManager.getBiometryStatus()) {
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
        SettingsUiState(
            fingerprintSection = fingerprintSection,
            themePreference = theme,
            autofillStatus = autofill,
            isLoadingState = loading,
            appVersion = appConfig.versionName
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SettingsUiState.getInitialState(appConfig.versionName)
    )

    fun onFingerPrintLockChange(contextHolder: ContextHolder, state: IsButtonEnabled) =
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
                            performFingerprintLockChange(state)
                        }
                        is BiometryResult.Error -> {
                            when (result.cause) {
                                // If the user has cancelled it, do nothing
                                BiometryAuthError.Canceled -> {}
                                BiometryAuthError.UserCanceled -> {}

                                else ->
                                    snackbarMessageRepository
                                        .emitSnackbarMessage(SettingsSnackbarMessage.BiometryFailedToAuthenticateError)
                            }
                        }

                        // User can retry
                        BiometryResult.Failed -> {}
                        is BiometryResult.FailedToStart ->
                            snackbarMessageRepository
                                .emitSnackbarMessage(SettingsSnackbarMessage.BiometryFailedToStartError)
                    }
                    PassLogger.i(TAG, "Biometry result: $result")
                }
                .collect { }
        }

    fun onThemePreferenceChange(theme: ThemePreference) = viewModelScope.launch {
        PassLogger.d(TAG, "Changing theme to $theme")
        preferencesRepository.setThemePreference(theme)
            .onFailure {
                PassLogger.e(TAG, it, "Error setting ThemePreference")
                snackbarMessageRepository.emitSnackbarMessage(SettingsSnackbarMessage.ErrorPerformingOperation)
            }
    }

    fun onToggleAutofill(value: Boolean) {
        if (value) {
            autofillManager.openAutofillSelector()
        } else {
            autofillManager.disableAutofill()
        }
    }

    fun onForceSync() = viewModelScope.launch {
        val userId = accountManager.getPrimaryUserId().firstOrNull() ?: return@launch

        isLoadingState.update { IsLoadingState.Loading }
        runCatching {
            refreshContent.invoke(userId)
        }.onSuccess {
            snackbarMessageRepository.emitSnackbarMessage(SettingsSnackbarMessage.SyncSuccessful)
        }.onFailure {
            PassLogger.i(TAG, it, "Error performing sync")
            snackbarMessageRepository.emitSnackbarMessage(SettingsSnackbarMessage.ErrorPerformingSync)
        }

        isLoadingState.update { IsLoadingState.NotLoading }
    }

    fun copyAppVersion(appVersion: String) = viewModelScope.launch {
        clipboardManager.copyToClipboard(appVersion, clearAfterSeconds = null)
        snackbarMessageRepository.emitSnackbarMessage(SettingsSnackbarMessage.AppVersionCopied)
    }

    private suspend fun performFingerprintLockChange(state: IsButtonEnabled) {
        val (lockState, message) = when (state) {
            IsButtonEnabled.Enabled -> BiometricLockState.Enabled to SettingsSnackbarMessage.FingerprintLockEnabled
            IsButtonEnabled.Disabled -> BiometricLockState.Disabled to SettingsSnackbarMessage.FingerprintLockDisabled
        }

        PassLogger.d(TAG, "Changing BiometricLock to $lockState")
        preferencesRepository.setBiometricLockState(lockState)
            .onSuccess {
                snackbarMessageRepository.emitSnackbarMessage(message)
            }
            .onFailure {
                PassLogger.e(TAG, it, "Error setting BiometricLockState")
                snackbarMessageRepository.emitSnackbarMessage(SettingsSnackbarMessage.ErrorPerformingOperation)
            }
    }

    private fun getFingerprintSection(biometricLock: Result<BiometricLockState>): BiometricLockState =
        when (biometricLock) {
            Result.Loading -> BiometricLockState.Disabled
            is Result.Success -> biometricLock.data
            is Result.Error -> {
                val message = "Error getting BiometricLock preference"
                PassLogger.e(TAG, biometricLock.exception ?: Exception(message), message)
                BiometricLockState.Disabled
            }
        }

    private fun getTheme(theme: Result<ThemePreference>): ThemePreference =
        when (theme) {
            Result.Loading -> ThemePreference.System
            is Result.Success -> theme.data
            is Result.Error -> {
                val message = "Error getting ThemePreference"
                PassLogger.e(TAG, theme.exception ?: Exception(message), message)
                ThemePreference.System
            }
        }

    companion object {
        private const val TAG = "SettingsViewModel"
    }
}
