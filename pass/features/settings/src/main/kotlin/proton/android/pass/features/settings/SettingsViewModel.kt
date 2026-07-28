/*
 * Copyright (c) 2023-2026 Proton AG
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
import android.os.Build
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.AppDispatchers
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.usersettings.domain.repository.DeviceSettingsRepository
import proton.android.pass.autofill.api.AutofillManager
import proton.android.pass.autofill.api.AutofillSupportedStatus
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.combineN
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.data.api.repositories.AssetLinkRepository
import proton.android.pass.data.api.repositories.ItemSyncStatusRepository
import proton.android.pass.data.api.usecases.InitialWorkerLauncher
import proton.android.pass.data.api.usecases.attachments.AttachmentDownloadScheduler
import proton.android.pass.data.api.usecases.PerformSync
import proton.android.pass.data.api.usecases.WorkerFeature
import proton.android.pass.image.api.ClearIconCache
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.AllowCellularDownloadPreference
import proton.android.pass.preferences.AllowScreenshotsPreference
import proton.android.pass.preferences.AutofillDisplayPreference
import proton.android.pass.preferences.CopyTotpToClipboard
import proton.android.pass.preferences.DownloadAllAttachmentsPreference
import proton.android.pass.preferences.ThemePreference
import proton.android.pass.preferences.UseDigitalAssetLinksPreference
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.domain.PlanType
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton.android.pass.preferences.UseFaviconsPreference
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.settings.AutosavePreference
import proton.android.pass.preferences.settings.SettingsDisplayAutofillPinningPreference
import proton.android.pass.preferences.settings.SettingsDisplayUsernameFieldPreference
import proton.android.pass.preferences.value
import proton.android.pass.telemetry.api.CanConfigureTelemetry
import proton.android.pass.data.api.usecases.attachments.EnableAllOfflineAttachments
import proton.android.pass.data.api.usecases.attachments.SetSharedItemsOfflineAttachments
import proton.android.pass.telemetry.api.TelemetryGrowthOptOutEvent
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

@HiltViewModel
@Suppress("LongParameterList")
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val performSync: PerformSync,
    private val accountManager: AccountManager,
    private val clearIconCache: ClearIconCache,
    private val deviceSettingsRepository: DeviceSettingsRepository,
    private val canConfigureTelemetry: CanConfigureTelemetry,
    private val initialWorkerLauncher: InitialWorkerLauncher,
    private val assetLinkRepository: AssetLinkRepository,
    private val enableAllOfflineAttachments: EnableAllOfflineAttachments,
    private val setSharedItemsOfflineAttachments: SetSharedItemsOfflineAttachments,
    private val attachmentDownloadScheduler: AttachmentDownloadScheduler,
    featureFlagsRepository: FeatureFlagsPreferencesRepository,
    getUserPlan: GetUserPlan,
    autofillManager: AutofillManager,
    private val syncStatusRepository: ItemSyncStatusRepository,
    private val telemetryManager: TelemetryManager,
    private val appDispatchers: AppDispatchers
) : ViewModel() {

    private val offlineAttachmentsFeatureEnabled: Flow<Boolean> =
        featureFlagsRepository.get<Boolean>(FeatureFlag.PASS_OFFLINE_ATTACHMENTS)
            .distinctUntilChanged()

    private val isOfflineAttachmentsPaidFeature: Flow<Boolean> = getUserPlan()
        .map { plan ->
            when (plan.planType) {
                is PlanType.Free, is PlanType.Unknown -> true
                is PlanType.Paid -> false
            }
        }
        .distinctUntilChanged()

    init {
        viewModelScope.launch {
            var previousValue: Boolean? = null
            offlineAttachmentsFeatureEnabled.collect { isEnabled ->
                if (previousValue == true && !isEnabled) {
                    onDownloadAllAttachmentsToggled(isEnabled = false)
                }
                previousValue = isEnabled
            }
        }
    }

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

    private val autofillStatusFlow: Flow<AutofillSupportedStatus> = autofillManager
        .getAutofillStatus()
        .distinctUntilChanged()

    private val autofillDisplayPreferenceFlow: Flow<AutofillDisplayPreference> =
        preferencesRepository.getAutofillDisplayPreference()
            .map { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) it else AutofillDisplayPreference.Popup }
            .distinctUntilChanged()

    private val autosavePreferenceFlow: Flow<AutosavePreference> =
        preferencesRepository
            .observeAutosavePreference()
            .distinctUntilChanged()

    private val downloadAllAttachmentsFlow: Flow<DownloadAllAttachmentsPreference> =
        accountManager.getPrimaryUserId().flatMapLatest { userId ->
            if (userId == null) flowOf(DownloadAllAttachmentsPreference.Disabled)
            else preferencesRepository.observeDownloadAllAttachmentsPref(userId)
        }.distinctUntilChanged()

    private val allowCellularDownloadFlow: Flow<AllowCellularDownloadPreference> =
        accountManager.getPrimaryUserId().flatMapLatest { userId ->
            if (userId == null) flowOf(AllowCellularDownloadPreference.Disabled)
            else preferencesRepository.observeAllowCellularDownloadPref(userId)
        }.distinctUntilChanged()

    private val eventState: MutableStateFlow<SettingsEvent> =
        MutableStateFlow(SettingsEvent.Unknown)

    private data class PreferencesState(
        val theme: ThemePreference,
        val copyTotpToClipboard: CopyTotpToClipboard,
        val useFavicons: UseFaviconsPreference,
        val useDigitalAssetLinks: UseDigitalAssetLinksPreference,
        val displayUsernameFieldPreference: SettingsDisplayUsernameFieldPreference,
        val displayAutofillPinningPreference: SettingsDisplayAutofillPinningPreference,
        val autofillDisplayPreference: AutofillDisplayPreference,
        val autosavePreference: AutosavePreference,
        val downloadAllAttachments: DownloadAllAttachmentsPreference,
        val allowCellularDownload: AllowCellularDownloadPreference
    )

    private val preferencesState: Flow<PreferencesState> = combineN(
        themeState,
        copyTotpToClipboardState,
        useFaviconsState,
        useDigitalAssetLinksState,
        displayUsernameFieldPreferenceFlow,
        preferencesRepository.observeDisplayAutofillPinningPreference(),
        autofillDisplayPreferenceFlow,
        autosavePreferenceFlow,
        downloadAllAttachmentsFlow,
        allowCellularDownloadFlow,
        ::PreferencesState
    )

    private val sharedItemsEnabledFlow = accountManager.getPrimaryUserId()
        .flatMapLatest { userId ->
            if (userId == null) flowOf(false)
            else preferencesRepository.observeSharedItemsDownloadPref(userId).map { it.value() }
        }
        .distinctUntilChanged()

    internal val state: StateFlow<SettingsUiState> = combineN(
        preferencesState,
        deviceSettingsRepository.observeDeviceSettings(),
        allowScreenshotsState,
        syncStatusRepository.observeSyncState().asLoadingResult(),
        eventState,
        autofillStatusFlow,
        sharedItemsEnabledFlow,
        offlineAttachmentsFeatureEnabled,
        isOfflineAttachmentsPaidFeature
    ) { preferences, deviceSettings, allowScreenshots,
        syncStateLoadingResult, event, autofillStatus, sharedItemsEnabled,
        isOfflineAttachmentsFeatureEnabled, isOfflineAttachmentsPaid ->
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
            displayAutofillPinningPreference = preferences.displayAutofillPinningPreference,
            autofillDisplayPreference = preferences.autofillDisplayPreference,
            autofillStatus = autofillStatus,
            autosavePreference = preferences.autosavePreference,
            downloadAllAttachments = preferences.downloadAllAttachments,
            sharedItemsEnabled = sharedItemsEnabled,
            allowCellularDownload = preferences.allowCellularDownload,
            isDownloadEnabled = preferences.downloadAllAttachments.value(),
            isOfflineAttachmentsFeatureEnabled = isOfflineAttachmentsFeatureEnabled,
            isOfflineAttachmentsPaidFeature = isOfflineAttachmentsPaid
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
            viewModelScope.launch(appDispatchers.io) {
                runCatching { assetLinkRepository.purgeAll() }
                    .onFailure {
                        PassLogger.w(TAG, "Error purging digital asset links")
                        PassLogger.w(TAG, it)
                    }
            }
        }
    }

    internal fun onAllowScreenshotsChange(allowScreenshots: Boolean) {
        preferencesRepository.setAllowScreenshotsPreference(
            preference = AllowScreenshotsPreference.from(allowScreenshots)
        )

        eventState.update { SettingsEvent.RestartApp }
    }

    internal fun onTelemetryChange(value: Boolean) = viewModelScope.launch {
        if (!value) {
            telemetryManager.sendEvent(TelemetryGrowthOptOutEvent)
        }
        deviceSettingsRepository.updateIsTelemetryEnabled(value)
        snackbarDispatcher(SettingsSnackbarMessage.PreferenceUpdated)
    }

    internal fun onCrashReportChange(value: Boolean) = viewModelScope.launch {
        deviceSettingsRepository.updateIsCrashReportEnabled(value)
        snackbarDispatcher(SettingsSnackbarMessage.PreferenceUpdated)
    }

    internal fun onForceSync() = viewModelScope.launch {
        val userId = accountManager.getPrimaryUserId().firstOrNull()
        if (userId != null) {
            safeRunCatching { performSync(userId, forceSync = true, trigger = "settings_manual") }
                .onFailure { error ->
                    PassLogger.w(TAG, "Error performing sync")
                    PassLogger.w(TAG, error)
                }
        } else {
            PassLogger.w(TAG, "Cannot perform sync: userId not available")
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

    internal fun onAutosaveChange(isEnabled: Boolean) {
        AutosavePreference.from(isEnabled)
            .also(preferencesRepository::setAutosavePreference)
    }

    internal fun onDownloadAllAttachmentsToggled(isEnabled: Boolean) {
        viewModelScope.launch {
            val userId = accountManager.getPrimaryUserId().firstOrNull()
            if (userId == null) {
                PassLogger.w(TAG, "Cannot toggle download attachments: userId not available")
                return@launch
            }
            preferencesRepository.setDownloadAllAttachmentsPref(
                userId,
                DownloadAllAttachmentsPreference.from(isEnabled)
            )
            if (isEnabled) {
                safeRunCatching {
                    enableAllOfflineAttachments(userId)
                }.onFailure { error ->
                    PassLogger.w(TAG, "Error enabling all offline attachments")
                    PassLogger.w(TAG, error)
                }
                eventState.update { SettingsEvent.OpenAttachmentConfigDialog }
            } else {
                safeRunCatching { attachmentDownloadScheduler.cancelAll(userId) }
                    .onFailure { error ->
                        PassLogger.w(TAG, "Error cancelling attachment downloads")
                        PassLogger.w(TAG, error)
                    }
            }
        }
    }

    internal fun onSharedItemsToggled(isEnabled: Boolean) {
        viewModelScope.launch {
            val userId = accountManager.getPrimaryUserId().firstOrNull() ?: return@launch
            preferencesRepository.setSharedItemsDownloadPref(
                userId,
                proton.android.pass.preferences.DownloadSharedItemsAttachmentsPreference.from(isEnabled)
            )
            safeRunCatching {
                setSharedItemsOfflineAttachments(userId, isEnabled)
            }.onFailure { error ->
                PassLogger.w(TAG, "Error toggling shared items offline attachments")
                PassLogger.w(TAG, error)
            }
            if (isEnabled) {
                safeRunCatching { attachmentDownloadScheduler.scheduleAll(userId, force = true) }
                    .onFailure { error ->
                        PassLogger.w(TAG, "Error scheduling shared items download")
                        PassLogger.w(TAG, error)
                    }
            } else {
                safeRunCatching { attachmentDownloadScheduler.cancelSharedItems(userId) }
                    .onFailure { error ->
                        PassLogger.w(TAG, "Error cancelling shared items download workers")
                        PassLogger.w(TAG, error)
                    }
            }
        }
    }

    internal fun onAllowCellularDownloadToggled(isEnabled: Boolean) {
        viewModelScope.launch {
            val userId = accountManager.getPrimaryUserId().firstOrNull() ?: return@launch
            preferencesRepository.setAllowCellularDownloadPref(
                userId,
                AllowCellularDownloadPreference.from(isEnabled)
            )
            safeRunCatching { attachmentDownloadScheduler.scheduleAll(userId, force = true) }
                .onFailure { error ->
                    PassLogger.w(TAG, "Error rescheduling downloads after cellular toggle")
                    PassLogger.w(TAG, error)
                }
        }
    }

    internal fun onEventConsumed(event: SettingsEvent) {
        eventState.compareAndSet(event, SettingsEvent.Unknown)
    }


    private companion object {

        private const val TAG = "SettingsViewModel"

    }

}
