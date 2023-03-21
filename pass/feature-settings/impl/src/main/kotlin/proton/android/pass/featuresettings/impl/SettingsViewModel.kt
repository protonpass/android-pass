package proton.android.pass.featuresettings.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getPrimaryAccount
import proton.android.pass.appconfig.api.AppConfig
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.RefreshContent
import proton.android.pass.featuresettings.impl.SettingsSnackbarMessage.ErrorPerformingOperation
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarMessageRepository
import proton.android.pass.preferences.BiometricLockState
import proton.android.pass.preferences.CopyTotpToClipboard
import proton.android.pass.preferences.ThemePreference
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val preferencesRepository: UserPreferencesRepository,
    private val snackbarMessageRepository: SnackbarMessageRepository,
    private val refreshContent: RefreshContent,
    private val clipboardManager: ClipboardManager,
    private val appConfig: AppConfig
) : ViewModel() {

    private val currentAccountState = accountManager.getPrimaryAccount()
        .filterNotNull()
        .distinctUntilChanged()

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

    data class PreferencesState(
        val biometricLock: BiometricLockState,
        val theme: ThemePreference,
        val copyTotpToClipboard: CopyTotpToClipboard
    )

    private val preferencesState: Flow<PreferencesState> = combine(
        biometricLockState,
        themeState,
        copyTotpToClipboardState
    ) { biometric, theme, totp -> PreferencesState(biometric, theme, totp) }

    private val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)

    val state: StateFlow<SettingsUiState> = combine(
        currentAccountState,
        preferencesState,
        isLoadingState
    ) { account, preferences, loading ->
        SettingsUiState(
            themePreference = preferences.theme,
            copyTotpToClipboard = preferences.copyTotpToClipboard,
            isLoadingState = loading,
            appVersion = appConfig.versionName,
            currentAccount = account.email ?: account.username
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SettingsUiState.getInitialState(appConfig.versionName)
    )

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
        withContext(Dispatchers.IO) {
            clipboardManager.copyToClipboard(appVersion, clearAfterSeconds = null)
        }
        snackbarMessageRepository.emitSnackbarMessage(SettingsSnackbarMessage.AppVersionCopied)
    }

    companion object {
        private const val TAG = "SettingsViewModel"
    }
}
