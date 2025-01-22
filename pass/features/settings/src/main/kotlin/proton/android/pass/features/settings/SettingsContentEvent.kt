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

internal sealed interface SettingsContentEvent {

    data class UseFaviconsChange(val value: Boolean) : SettingsContentEvent

    data class UseDigitalAssetLinksChange(val value: Boolean) : SettingsContentEvent

    data class AllowScreenshotsChange(val value: Boolean) : SettingsContentEvent

    data class TelemetryChange(val value: Boolean) : SettingsContentEvent

    data class CrashReportChange(val value: Boolean) : SettingsContentEvent

    data object SelectTheme : SettingsContentEvent

    data object Clipboard : SettingsContentEvent

    data object ViewLogs : SettingsContentEvent

    data object ForceSync : SettingsContentEvent

    data object Privacy : SettingsContentEvent

    data object Terms : SettingsContentEvent

    data object Up : SettingsContentEvent

    @JvmInline
    value class OnDisplayUsernameToggled(val isEnabled: Boolean) : SettingsContentEvent

    @JvmInline
    value class OnDisplayAutofillPinningToggled(val isEnabled: Boolean) : SettingsContentEvent

}
