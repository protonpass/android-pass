/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.initializer.theme

import androidx.appcompat.app.AppCompatDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.preferences.ThemePreference
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject

class ThemeObserver @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val appDispatchers: AppDispatchers
) {

    fun start() {
        userPreferencesRepository.getThemePreference()
            .onEach { applyDefaultTheme(it) }
            .launchIn(CoroutineScope(appDispatchers.io + SupervisorJob()))
    }

    private suspend fun applyDefaultTheme(theme: ThemePreference) = when (theme) {
        ThemePreference.Light -> setNightModeDisabled()
        ThemePreference.Dark -> setNightModeEnabled()
        ThemePreference.System -> setNightModeFollowSystem()
    }

    private suspend fun setNightModeFollowSystem() = withContext(Dispatchers.Main) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    private suspend fun setNightModeEnabled() = withContext(Dispatchers.Main) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }

    private suspend fun setNightModeDisabled() = withContext(Dispatchers.Main) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}
