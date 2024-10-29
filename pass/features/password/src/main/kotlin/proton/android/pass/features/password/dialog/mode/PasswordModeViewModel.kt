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

package proton.android.pass.features.password.dialog.mode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.some
import proton.android.pass.preferences.PasswordGenerationMode
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class PasswordModeViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val passwordModeOption = userPreferencesRepository.getPasswordGenerationPreference()
        .mapLatest { passwordGenerationPreference -> passwordGenerationPreference.mode.some() }

    private val eventFlow = MutableStateFlow<PasswordModeUiEvent>(PasswordModeUiEvent.Idle)

    internal val stateFlow: StateFlow<PasswordModeUiState> = combine(
        passwordModeOption,
        eventFlow,
        ::PasswordModeUiState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = PasswordModeUiState.Initial
    )

    internal fun onUpdatePasswordGenerationMode(newMode: PasswordGenerationMode) {
        viewModelScope.launch {
            userPreferencesRepository.getPasswordGenerationPreference()
                .first()
                .copy(mode = newMode)
                .also(userPreferencesRepository::setPasswordGenerationPreference)

            eventFlow.update { PasswordModeUiEvent.Close }
        }
    }

}
