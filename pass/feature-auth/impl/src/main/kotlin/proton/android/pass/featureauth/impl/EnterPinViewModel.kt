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

package proton.android.pass.featureauth.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import proton.android.pass.featureauth.impl.EnterPinUiState.NotInitialised
import proton.android.pass.preferences.InternalSettingsRepository
import javax.inject.Inject

@HiltViewModel
class EnterPinViewModel @Inject constructor(
    internalSettingsRepository: InternalSettingsRepository
) : ViewModel() {

    private val pinState: MutableStateFlow<String> = MutableStateFlow("")
    val state: StateFlow<EnterPinUiState> = combine(
        pinState,
        internalSettingsRepository.getPinAttemptsCount(),
        EnterPinUiState::Data
    )
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = NotInitialised
        )

    fun onPinChanged(value: String) {
        val sanitisedValue = value.replace(nonDigitRegex, "").take(MAX_PIN_LENGTH)
        pinState.update { sanitisedValue }
    }

    fun onPinSubmit() {
        // Check and increment or clear attempts
    }

    companion object {
        private const val MAX_PIN_LENGTH = 100
        val nonDigitRegex: Regex = "\\D".toRegex()
    }
}
