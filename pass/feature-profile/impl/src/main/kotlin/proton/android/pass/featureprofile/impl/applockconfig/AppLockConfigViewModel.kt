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

package proton.android.pass.featureprofile.impl.applockconfig

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import proton.android.pass.preferences.AppLockTypePreference
import proton.android.pass.preferences.BiometricSystemLockPreference
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class AppLockConfigViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _state: Flow<AppLockConfigUiState> = combine(
        userPreferencesRepository.getAppLockTimePreference(),
        userPreferencesRepository.getAppLockTypePreference(),
        userPreferencesRepository.getBiometricSystemLockPreference()
    ) { time, type, biometricSystemLock ->
        when (type) {
            AppLockTypePreference.Biometrics -> {
                AppLockConfigUiState.Biometric(time, biometricSystemLock)
            }

            AppLockTypePreference.Pin -> AppLockConfigUiState.Pin(time)
        }

    }
    val state: StateFlow<AppLockConfigUiState> = _state
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = runBlocking { _state.first() }
        )

    fun onToggleBiometricSystemLock(value: Boolean) = viewModelScope.launch {
        userPreferencesRepository.setBiometricSystemLockPreference(
            BiometricSystemLockPreference.from(value)
        )
    }
}
