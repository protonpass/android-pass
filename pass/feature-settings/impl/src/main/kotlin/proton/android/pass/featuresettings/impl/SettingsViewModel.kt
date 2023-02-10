package proton.android.pass.featuresettings.impl

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
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.appconfig.api.AppConfig
import proton.android.pass.autofill.api.AutofillManager
import proton.android.pass.autofill.api.AutofillSupportedStatus
import proton.android.pass.biometry.BiometryAuthError
import proton.android.pass.biometry.BiometryManager
import proton.android.pass.biometry.BiometryResult
import proton.android.pass.biometry.BiometryStatus
import proton.android.pass.biometry.ContextHolder
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.RefreshContent
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarMessageRepository
import proton.android.pass.preferences.BiometricLockState
import proton.android.pass.preferences.CopyTotpToClipboard
import proton.android.pass.preferences.HasAuthenticated
import proton.android.pass.preferences.ThemePreference
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.featuresettings.impl.SettingsSnackbarMessage.ErrorPerformingOperation
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
        .distinctUntilChanged()

    private val themeState: Flow<ThemePreference> = preferencesRepository
        .getThemePreference()
        .distinctUntilChanged()

    private val copyTotpToClipboardState: Flow<CopyTotpToClipboard> =
        preferencesRepository
            .getCopyTotpToClipboardEnabled()
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
        copyTotpToClipboardState,
        isLoadingState
    ) { biometricLock, theme, autofill, copyTotpToClipboardEnabled, loading ->
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
            copyTotpToClipboard = copyTotpToClipboardEnabled,
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
                snackbarMessageRepository.emitSnackbarMessage(ErrorPerformingOperation)
            }
    }

    fun onToggleAutofill(value: Boolean) {
        if (value) {
            autofillManager.openAutofillSelector()
        } else {
            autofillManager.disableAutofill()
        }
    }

    fun onCopyToClipboardChange(value: Boolean) = viewModelScope.launch {
        PassLogger.d(TAG, "Changing CopyTotpToClipboard to $value")
        preferencesRepository.setCopyTotpToClipboardEnabled(CopyTotpToClipboard.from(value))
            .onFailure {
                PassLogger.e(TAG, it, "Error setting CopyTotpToClipboard")
                snackbarMessageRepository.emitSnackbarMessage(ErrorPerformingOperation)
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
            PassLogger.e(TAG, it, "Error performing sync")
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
                snackbarMessageRepository.emitSnackbarMessage(ErrorPerformingOperation)
            }
    }

    companion object {
        private const val TAG = "SettingsViewModel"
    }
}
