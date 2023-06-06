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
import me.proton.core.usersettings.domain.repository.DeviceSettingsRepository
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.combineN
import proton.android.pass.common.api.toOption
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.android.pass.data.api.usecases.RefreshContent
import proton.android.pass.image.api.ClearIconCache
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.AllowScreenshotsPreference
import proton.android.pass.preferences.CopyTotpToClipboard
import proton.android.pass.preferences.ThemePreference
import proton.android.pass.preferences.UseFaviconsPreference
import proton.android.pass.preferences.UserPreferencesRepository
import proton.pass.domain.Vault
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val observeCurrentUser: ObserveCurrentUser,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val refreshContent: RefreshContent,
    private val clearIconCache: ClearIconCache,
    private val deviceSettingsRepository: DeviceSettingsRepository,
    observeVaults: ObserveVaults
) : ViewModel() {

    private val themeState: Flow<ThemePreference> = preferencesRepository
        .getThemePreference()
        .distinctUntilChanged()

    private val copyTotpToClipboardState: Flow<CopyTotpToClipboard> =
        preferencesRepository
            .getCopyTotpToClipboardEnabled()
            .distinctUntilChanged()

    private val useFaviconsState: Flow<UseFaviconsPreference> =
        preferencesRepository
            .getUseFaviconsPreference()
            .distinctUntilChanged()

    private val allowScreenshotsState: Flow<AllowScreenshotsPreference> =
        preferencesRepository
            .getAllowScreenshotsPreference()
            .distinctUntilChanged()

    private val eventState: MutableStateFlow<SettingsEvent> =
        MutableStateFlow(SettingsEvent.Unknown)

    data class PreferencesState(
        val theme: ThemePreference,
        val copyTotpToClipboard: CopyTotpToClipboard,
        val useFavicons: UseFaviconsPreference,
    )

    private val preferencesState: Flow<PreferencesState> = combine(
        themeState,
        copyTotpToClipboardState,
        useFaviconsState
    ) { theme, totp, favicons -> PreferencesState(theme, totp, favicons) }

    private val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)

    private val primaryVaultFlow: Flow<PrimaryVaultWrapper> = observeVaults()
        .asLoadingResult()
        .map { res ->
            when (res) {
                LoadingResult.Loading -> PrimaryVaultWrapper(primaryVault = None, showSelector = false)
                is LoadingResult.Error -> {
                    PassLogger.e(TAG, res.exception, "Error observing vaults")
                    PrimaryVaultWrapper(primaryVault = None, showSelector = false)
                }
                is LoadingResult.Success -> {
                    val primary = res.data.firstOrNull { it.isPrimary }
                        ?: res.data.firstOrNull()

                    PrimaryVaultWrapper(
                        primaryVault = primary.toOption(),
                        showSelector = res.data.size > 1
                    )

                }
            }
        }

    private data class PrimaryVaultWrapper(
        val primaryVault: Option<Vault>,
        val showSelector: Boolean
    )

    val state: StateFlow<SettingsUiState> = combineN(
        preferencesState,
        primaryVaultFlow,
        isLoadingState,
        deviceSettingsRepository.observeDeviceSettings(),
        allowScreenshotsState,
        eventState
    ) { preferences, primaryVault, loading, deviceSettings, allowScreenshots, event ->
        SettingsUiState(
            themePreference = preferences.theme,
            copyTotpToClipboard = preferences.copyTotpToClipboard,
            isLoadingState = loading,
            primaryVault = primaryVault.primaryVault,
            showPrimaryVaultSelector = primaryVault.showSelector,
            useFavicons = preferences.useFavicons,
            allowScreenshots = allowScreenshots,
            shareTelemetry = deviceSettings.isTelemetryEnabled,
            shareCrashes = deviceSettings.isCrashReportEnabled,
            event = event
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SettingsUiState.Initial
    )

    fun onUseFaviconsChange(useFavicons: Boolean) = viewModelScope.launch {
        preferencesRepository.setUseFaviconsPreference(UseFaviconsPreference.from(useFavicons))

        if (!useFavicons) {
            runCatching { clearIconCache() }
                .onSuccess {
                    snackbarDispatcher(SettingsSnackbarMessage.ClearIconCacheSuccess)
                }
                .onFailure {
                    PassLogger.w(TAG, it, "Error clearing icon cache")
                    snackbarDispatcher(SettingsSnackbarMessage.ClearIconCacheError)
                }
        }
    }

    fun onAllowScreenshotsChange(allowScreenshots: Boolean) = viewModelScope.launch {
        preferencesRepository.setAllowScreenshotsPreference(AllowScreenshotsPreference.from(allowScreenshots))
        eventState.update { SettingsEvent.RestartApp }
    }

    fun onTelemetryChange(value: Boolean) = viewModelScope.launch {
        deviceSettingsRepository.updateIsTelemetryEnabled(value)
        snackbarDispatcher(SettingsSnackbarMessage.PreferenceUpdated)
    }

    fun onCrashReportChange(value: Boolean) = viewModelScope.launch {
        deviceSettingsRepository.updateIsCrashReportEnabled(value)
        snackbarDispatcher(SettingsSnackbarMessage.PreferenceUpdated)
    }

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
