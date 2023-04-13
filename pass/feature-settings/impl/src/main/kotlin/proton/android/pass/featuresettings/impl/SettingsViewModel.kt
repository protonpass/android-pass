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
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.android.pass.data.api.usecases.RefreshContent
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.BiometricLockState
import proton.android.pass.preferences.CopyTotpToClipboard
import proton.android.pass.preferences.ThemePreference
import proton.android.pass.preferences.UserPreferencesRepository
import proton.pass.domain.Vault
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    preferencesRepository: UserPreferencesRepository,
    private val observeCurrentUser: ObserveCurrentUser,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val refreshContent: RefreshContent,
    observeVaults: ObserveVaults
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

    private val primaryVaultFlow: Flow<Option<Vault>> = observeVaults()
        .map { res ->
            when (res) {
                LoadingResult.Loading -> None
                is LoadingResult.Error -> {
                    PassLogger.e(TAG, res.exception, "Error observing vaults")
                    None
                }
                is LoadingResult.Success -> {
                    val primary = res.data.firstOrNull { it.isPrimary }
                        ?: res.data.firstOrNull()
                    primary.toOption()
                }
            }
        }

    val state: StateFlow<SettingsUiState> = combine(
        preferencesState,
        primaryVaultFlow,
        isLoadingState
    ) { preferences, primaryVault, loading ->
        SettingsUiState(
            themePreference = preferences.theme,
            copyTotpToClipboard = preferences.copyTotpToClipboard,
            isLoadingState = loading,
            primaryVault = primaryVault
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SettingsUiState.Initial
    )

    fun onForceSync() = viewModelScope.launch {
        val userId = observeCurrentUser().firstOrNull()?.userId ?: return@launch

        isLoadingState.update { IsLoadingState.Loading }
        runCatching {
            refreshContent.invoke(userId)
        }.onSuccess {
            snackbarDispatcher(SettingsSnackbarMessage.SyncSuccessful)
        }.onFailure {
            PassLogger.e(TAG, it, "Error performing sync")
            snackbarDispatcher(SettingsSnackbarMessage.ErrorPerformingSync)
        }

        isLoadingState.update { IsLoadingState.NotLoading }
    }

    companion object {
        private const val TAG = "SettingsViewModel"
    }
}
