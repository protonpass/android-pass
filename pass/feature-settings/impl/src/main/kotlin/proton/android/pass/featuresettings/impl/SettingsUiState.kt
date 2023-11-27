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

import androidx.compose.runtime.Stable
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.preferences.AllowScreenshotsPreference
import proton.android.pass.preferences.CopyTotpToClipboard
import proton.android.pass.preferences.ThemePreference
import proton.android.pass.preferences.UseFaviconsPreference

sealed interface SettingsEvent {
    object Unknown : SettingsEvent
    object RestartApp : SettingsEvent
}

@Stable
data class SettingsUiState(
    val themePreference: ThemePreference,
    val copyTotpToClipboard: CopyTotpToClipboard,
    val isForceRefreshing: Boolean,
    val useFavicons: UseFaviconsPreference,
    val allowScreenshots: AllowScreenshotsPreference,
    val shareTelemetry: Boolean,
    val shareCrashes: Boolean,
    val event: SettingsEvent,
    val defaultVault: Option<VaultWithItemCount>
) {
    companion object {
        val Initial = SettingsUiState(
            themePreference = ThemePreference.System,
            copyTotpToClipboard = CopyTotpToClipboard.NotEnabled,
            isForceRefreshing = false,
            useFavicons = UseFaviconsPreference.Enabled,
            allowScreenshots = AllowScreenshotsPreference.Disabled,
            shareTelemetry = true,
            shareCrashes = true,
            event = SettingsEvent.Unknown,
            defaultVault = None
        )
    }
}
