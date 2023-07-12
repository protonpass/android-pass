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

package proton.android.pass.featureprofile.impl.applocktype

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.preferences.AppLockTypePreference
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class AppLockTypeViewModel @Inject constructor(
    userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val eventState: MutableStateFlow<AppLockTypeEvent> =
        MutableStateFlow(AppLockTypeEvent.Unknown)

    val state: StateFlow<AppLockTypeUiState> = combine(
        userPreferencesRepository.getAppLockTypePreference(),
        eventState
    ) { preference, event ->
        AppLockTypeUiState(
            items = allPreferences,
            selected = preference,
            event = event
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppLockTypeUiState.Initial
    )

    fun onChanged(appLockTypePreference: AppLockTypePreference) = viewModelScope.launch {
        if (state.value.selected == appLockTypePreference) {
            eventState.update { AppLockTypeEvent.Dismiss }
        } else {
            eventState.update { AppLockTypeEvent.OnChanged(appLockTypePreference) }
        }
    }

    fun clearEvents() = viewModelScope.launch {
        eventState.update { AppLockTypeEvent.Unknown }
    }
}
