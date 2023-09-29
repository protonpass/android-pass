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
import proton.android.pass.data.api.repositories.ItemSyncStatus
import proton.android.pass.data.api.repositories.ItemSyncStatusRepository
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.android.pass.data.api.usecases.RefreshContent
import proton.android.pass.image.api.ClearIconCache
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.AllowScreenshotsPreference
import proton.android.pass.preferences.CopyTotpToClipboard
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton.android.pass.preferences.ThemePreference
import proton.android.pass.preferences.UseFaviconsPreference
import proton.android.pass.preferences.UserPreferencesRepository
import proton.pass.domain.Vault
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val refreshContent: RefreshContent,
    private val clearIconCache: ClearIconCache,
    private val deviceSettingsRepository: DeviceSettingsRepository,
    syncStatusRepository: ItemSyncStatusRepository,
    observeVaults: ObserveVaults,
    ffRepo: FeatureFlagsPreferencesRepository
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

    private val primaryVaultFlow: Flow<PrimaryVaultWrapper> = combine(
        observeVaults().asLoadingResult(),
        ffRepo.get<Boolean>(FeatureFlag.REMOVE_PRIMARY_VAULT)
    ) { res, removePrimaryVault ->
        if (removePrimaryVault) {
            return@combine PrimaryVaultWrapper(primaryVault = None, showSelector = false)
        }
        when (res) {
            LoadingResult.Loading -> PrimaryVaultWrapper(
                primaryVault = None,
                showSelector = false
            )

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
        deviceSettingsRepository.observeDeviceSettings(),
        allowScreenshotsState,
        syncStatusRepository.observeSyncStatus(),
        eventState
    ) { preferences, primaryVault, deviceSettings, allowScreenshots, sync, event ->
        SettingsUiState(
            themePreference = preferences.theme,
            copyTotpToClipboard = preferences.copyTotpToClipboard,
            primaryVault = primaryVault.primaryVault,
            showPrimaryVaultSelector = primaryVault.showSelector,
            isForceRefreshing = sync is ItemSyncStatus.Started || sync is ItemSyncStatus.Syncing,
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
        preferencesRepository.setAllowScreenshotsPreference(
            AllowScreenshotsPreference.from(
                allowScreenshots
            )
        )
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
        runCatching { refreshContent() }
            .onFailure {
                PassLogger.e(TAG, it, "Error performing sync")
            }
    }

    companion object {
        private const val TAG = "SettingsViewModel"
    }
}
