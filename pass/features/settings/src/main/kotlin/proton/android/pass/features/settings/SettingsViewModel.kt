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

package proton.android.pass.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.usersettings.domain.repository.DeviceSettingsRepository
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.combineN
import proton.android.pass.data.api.repositories.ItemSyncStatusRepository
import proton.android.pass.data.api.usecases.InitialWorkerLauncher
import proton.android.pass.data.api.usecases.RefreshContent
import proton.android.pass.data.api.usecases.WorkerFeature
import proton.android.pass.image.api.ClearIconCache
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.AllowScreenshotsPreference
import proton.android.pass.preferences.CopyTotpToClipboard
import proton.android.pass.preferences.ThemePreference
import proton.android.pass.preferences.UseDigitalAssetLinksPreference
import proton.android.pass.preferences.UseFaviconsPreference
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.settings.SettingsDisplayAutofillPinningPreference
import proton.android.pass.preferences.settings.SettingsDisplayUsernameFieldPreference
import proton.android.pass.telemetry.api.CanConfigureTelemetry
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val refreshContent: RefreshContent,
    private val clearIconCache: ClearIconCache,
    private val deviceSettingsRepository: DeviceSettingsRepository,
    private val canConfigureTelemetry: CanConfigureTelemetry,
    private val initialWorkerLauncher: InitialWorkerLauncher,
    syncStatusRepository: ItemSyncStatusRepository
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

    private val useDigitalAssetLinksState: Flow<UseDigitalAssetLinksPreference> =
        preferencesRepository
            .observeUseDigitalAssetLinksPreference()
            .distinctUntilChanged()

    private val allowScreenshotsState: Flow<AllowScreenshotsPreference> =
        preferencesRepository
            .getAllowScreenshotsPreference()
            .distinctUntilChanged()

    private val displayUsernameFieldPreferenceFlow: Flow<SettingsDisplayUsernameFieldPreference> =
        preferencesRepository
            .observeDisplayUsernameFieldPreference()
            .distinctUntilChanged()

    private val eventState: MutableStateFlow<SettingsEvent> =
        MutableStateFlow(SettingsEvent.Unknown)

    private data class PreferencesState(
        val theme: ThemePreference,
        val copyTotpToClipboard: CopyTotpToClipboard,
        val useFavicons: UseFaviconsPreference,
        val useDigitalAssetLinks: UseDigitalAssetLinksPreference,
        val displayUsernameFieldPreference: SettingsDisplayUsernameFieldPreference,
        val displayAutofillPinningPreference: SettingsDisplayAutofillPinningPreference
    )

    private val preferencesState: Flow<PreferencesState> = combineN(
        themeState,
        copyTotpToClipboardState,
        useFaviconsState,
        useDigitalAssetLinksState,
        displayUsernameFieldPreferenceFlow,
        preferencesRepository.observeDisplayAutofillPinningPreference(),
        ::PreferencesState
    )

    internal val state: StateFlow<SettingsUiState> = combine(
        preferencesState,
        deviceSettingsRepository.observeDeviceSettings(),
        allowScreenshotsState,
        syncStatusRepository.observeSyncState().asLoadingResult(),
        eventState
    ) { preferences, deviceSettings, allowScreenshots, syncStateLoadingResult, event ->
        val telemetryStatus = if (canConfigureTelemetry()) {
            TelemetryStatus.Show(
                shareTelemetry = deviceSettings.isTelemetryEnabled,
                shareCrashes = deviceSettings.isCrashReportEnabled
            )
        } else {
            TelemetryStatus.Hide
        }

        SettingsUiState(
            themePreference = preferences.theme,
            copyTotpToClipboard = preferences.copyTotpToClipboard,
            syncStateLoadingResult = syncStateLoadingResult,
            useFavicons = preferences.useFavicons,
            useDigitalAssetLinks = preferences.useDigitalAssetLinks,
            allowScreenshots = allowScreenshots,
            telemetryStatus = telemetryStatus,
            event = event,
            displayUsernameFieldPreference = preferences.displayUsernameFieldPreference,
            displayAutofillPinningPreference = preferences.displayAutofillPinningPreference
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SettingsUiState.Initial
    )

    internal fun onUseFaviconsChange(useFavicons: Boolean) = viewModelScope.launch {
        preferencesRepository.setUseFaviconsPreference(UseFaviconsPreference.from(useFavicons))

        if (!useFavicons) {
            runCatching { clearIconCache() }
                .onSuccess {
                    snackbarDispatcher(SettingsSnackbarMessage.ClearIconCacheSuccess)
                }
                .onFailure {
                    PassLogger.w(TAG, "Error clearing icon cache")
                    PassLogger.w(TAG, it)
                    snackbarDispatcher(SettingsSnackbarMessage.ClearIconCacheError)
                }
        }
    }

    internal fun onUseDigitalAssetLinksChange(useDigitalAssetLinks: Boolean) {
        preferencesRepository.setUseDigitalAssetLinksPreference(
            preference = UseDigitalAssetLinksPreference.from(useDigitalAssetLinks)
        )
        if (!useDigitalAssetLinks) {
            initialWorkerLauncher.cancelFeature(WorkerFeature.ASSET_LINKS)
        }
    }

    internal fun onAllowScreenshotsChange(allowScreenshots: Boolean) {
        preferencesRepository.setAllowScreenshotsPreference(
            preference = AllowScreenshotsPreference.from(allowScreenshots)
        )

        eventState.update { SettingsEvent.RestartApp }
    }

    internal fun onTelemetryChange(value: Boolean) = viewModelScope.launch {
        deviceSettingsRepository.updateIsTelemetryEnabled(value)
        snackbarDispatcher(SettingsSnackbarMessage.PreferenceUpdated)
    }

    internal fun onCrashReportChange(value: Boolean) = viewModelScope.launch {
        deviceSettingsRepository.updateIsCrashReportEnabled(value)
        snackbarDispatcher(SettingsSnackbarMessage.PreferenceUpdated)
    }

    internal fun onForceSync() = viewModelScope.launch {
        runCatching { refreshContent() }
            .onFailure { error ->
                PassLogger.w(TAG, "Error performing sync")
                PassLogger.w(TAG, error)
            }
    }

    internal fun onToggleDisplayUsernameField(isEnabled: Boolean) {
        SettingsDisplayUsernameFieldPreference.from(isEnabled)
            .also(preferencesRepository::setDisplayUsernameFieldPreference)
    }

    fun onToggleDisplayAutofillPinning(isEnabled: Boolean) {
        SettingsDisplayAutofillPinningPreference.from(isEnabled)
            .also(preferencesRepository::setDisplayAutofillPinningPreference)
    }

    private companion object {

        private const val TAG = "SettingsViewModel"

    }

}
