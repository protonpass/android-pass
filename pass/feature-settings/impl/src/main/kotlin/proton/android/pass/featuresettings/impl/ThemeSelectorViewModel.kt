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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import proton.android.pass.featuresettings.impl.SettingsSnackbarMessage.ErrorPerformingOperation
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.ThemePreference
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class ThemeSelectorViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val snackbarDispatcher: SnackbarDispatcher
) : ViewModel() {

    val state = preferencesRepository.getThemePreference()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = runBlocking { preferencesRepository.getThemePreference().first() }
        )

    fun onThemePreferenceChange(theme: ThemePreference) = viewModelScope.launch {
        PassLogger.d(TAG, "Changing theme to $theme")
        preferencesRepository.setThemePreference(theme)
            .onFailure {
                PassLogger.e(TAG, it, "Error setting ThemePreference")
                snackbarDispatcher(ErrorPerformingOperation)
            }
    }

    companion object {
        private const val TAG = "ThemeSelectorViewModel"
    }
}
