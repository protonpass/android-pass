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

package proton.android.pass.featurepassword.impl.dialog.separator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.some
import proton.android.pass.commonrust.api.WordSeparator
import proton.android.pass.featurepassword.impl.extensions.toDomain
import proton.android.pass.featurepassword.impl.extensions.toPassword
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class WordSeparatorViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val selectedSeparatorFlow =
        MutableStateFlow<Option<WordSeparator>>(None)
    private val eventFlow = MutableStateFlow<WordSeparatorUiEvent>(WordSeparatorUiEvent.Unknown)

    private val preferenceFlow = userPreferencesRepository.getPasswordGenerationPreference()
        .onEach { pref ->
            if (selectedSeparatorFlow.value is None) {
                selectedSeparatorFlow.update { pref.wordsSeparator.toDomain().some() }
            }
        }

    val state: StateFlow<WordSeparatorUiState> = combine(
        selectedSeparatorFlow,
        preferenceFlow,
        eventFlow
    ) { selectedSeparator, preference, event ->
        val selected = when (selectedSeparator) {
            None -> preference.wordsSeparator.toDomain().some()
            else -> selectedSeparator
        }

        WordSeparatorUiState(
            options = WordSeparatorUiState.Initial.options,
            selected = selected,
            event = event
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = WordSeparatorUiState.Initial
        )

    fun onChange(value: WordSeparator) {
        selectedSeparatorFlow.update { value.some() }
    }

    fun onConfirm() = viewModelScope.launch {
        val current = userPreferencesRepository.getPasswordGenerationPreference().first()
        val selectedSeparator = selectedSeparatorFlow.value
        if (selectedSeparator is Some) {
            val updated = current.copy(wordsSeparator = selectedSeparator.value.toPassword())
            userPreferencesRepository.setPasswordGenerationPreference(updated)
        }

        eventFlow.update { WordSeparatorUiEvent.Close }
    }

}
