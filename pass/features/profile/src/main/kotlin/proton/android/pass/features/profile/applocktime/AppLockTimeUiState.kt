/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.profile.applocktime

import proton.android.pass.preferences.AppLockTimePreference

sealed interface AppLockTimeEvent {
    data object OnChanged : AppLockTimeEvent
    data object Unknown : AppLockTimeEvent
}

data class AppLockTimeUiState(
    val items: List<AppLockTimePreference>,
    val selected: AppLockTimePreference,
    val event: AppLockTimeEvent
) {
    companion object {
        val Initial = AppLockTimeUiState(
            items = allPreferences,
            selected = AppLockTimePreference.InTwoMinutes,
            event = AppLockTimeEvent.Unknown
        )
    }
}

internal val allPreferences: List<AppLockTimePreference> = listOf(
    AppLockTimePreference.Immediately,
    AppLockTimePreference.InOneMinute,
    AppLockTimePreference.InTwoMinutes,
    AppLockTimePreference.InFiveMinutes,
    AppLockTimePreference.InTenMinutes,
    AppLockTimePreference.InOneHour,
    AppLockTimePreference.InFourHours
)
