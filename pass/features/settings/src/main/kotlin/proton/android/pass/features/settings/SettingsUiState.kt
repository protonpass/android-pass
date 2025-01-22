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

import androidx.compose.runtime.Stable
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.data.api.repositories.SyncState
import proton.android.pass.preferences.AllowScreenshotsPreference
import proton.android.pass.preferences.CopyTotpToClipboard
import proton.android.pass.preferences.ThemePreference
import proton.android.pass.preferences.UseDigitalAssetLinksPreference
import proton.android.pass.preferences.UseFaviconsPreference
import proton.android.pass.preferences.settings.SettingsDisplayAutofillPinningPreference
import proton.android.pass.preferences.settings.SettingsDisplayUsernameFieldPreference

internal sealed interface SettingsEvent {

    data object Unknown : SettingsEvent

    data object RestartApp : SettingsEvent

}

internal sealed interface TelemetryStatus {

    data object Hide : TelemetryStatus

    data class Show(
        val shareTelemetry: Boolean,
        val shareCrashes: Boolean
    ) : TelemetryStatus

}

@Stable
internal data class SettingsUiState(
    val themePreference: ThemePreference,
    val copyTotpToClipboard: CopyTotpToClipboard,
    val useFavicons: UseFaviconsPreference,
    val isDigitalAssetLinkEnabled: Boolean,
    val useDigitalAssetLinks: UseDigitalAssetLinksPreference,
    val allowScreenshots: AllowScreenshotsPreference,
    val telemetryStatus: TelemetryStatus,
    val event: SettingsEvent,
    val displayUsernameFieldPreference: SettingsDisplayUsernameFieldPreference,
    val displayAutofillPinningPreference: SettingsDisplayAutofillPinningPreference,
    private val syncStateLoadingResult: LoadingResult<SyncState>
) {

    internal val isForceRefreshing: Boolean = when (syncStateLoadingResult) {
        is LoadingResult.Error,
        LoadingResult.Loading -> false

        is LoadingResult.Success -> with(syncStateLoadingResult.data) {
            isSyncing && isVisibleSyncing
        }
    }

    internal companion object {

        internal val Initial = SettingsUiState(
            themePreference = ThemePreference.System,
            copyTotpToClipboard = CopyTotpToClipboard.NotEnabled,
            syncStateLoadingResult = LoadingResult.Loading,
            useFavicons = UseFaviconsPreference.Enabled,
            isDigitalAssetLinkEnabled = false,
            useDigitalAssetLinks = UseDigitalAssetLinksPreference.Enabled,
            allowScreenshots = AllowScreenshotsPreference.Disabled,
            telemetryStatus = TelemetryStatus.Hide,
            event = SettingsEvent.Unknown,
            displayUsernameFieldPreference = SettingsDisplayUsernameFieldPreference.Disabled,
            displayAutofillPinningPreference = SettingsDisplayAutofillPinningPreference.Disabled
        )

    }

}
