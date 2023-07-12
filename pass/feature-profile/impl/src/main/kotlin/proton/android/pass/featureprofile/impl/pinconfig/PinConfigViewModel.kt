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

package proton.android.pass.featureprofile.impl.pinconfig

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import proton.android.pass.common.api.CommonRegex.NON_DIGIT_REGEX
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import javax.inject.Inject

@HiltViewModel
class PinConfigViewModel @Inject constructor() : ViewModel() {

    private val _state: MutableStateFlow<PinConfigUiState> = MutableStateFlow(PinConfigUiState())

    val state: StateFlow<PinConfigUiState> = _state

    fun onEnterPin(value: String) {
        val sanitisedValue = value.replace(NON_DIGIT_REGEX, "").take(PIN_LENGTH)
        _state.update { it.copy(pin = sanitisedValue) }
    }

    fun onRepeatPin(value: String) {
        val sanitisedValue = value.replace(NON_DIGIT_REGEX, "").take(PIN_LENGTH)
        _state.update { it.copy(repeatPin = sanitisedValue) }
    }

    fun onSubmit() {
        _state.update { it.copy(isLoading = IsLoadingState.Loading) }
        val currentState = _state.value
        if (currentState.pin.isBlank()) {
            _state.update { it.copy(validationErrors = persistentSetOf(PinConfigValidationErrors.PinBlank)) }
        } else if (currentState.pin != currentState.repeatPin) {
            _state.update { it.copy(validationErrors = persistentSetOf(PinConfigValidationErrors.PinDoesNotMatch)) }
        } else {
            _state.update { it.copy(validationErrors = persistentSetOf()) }
        }
        _state.update { it.copy(isLoading = IsLoadingState.NotLoading) }
    }

    companion object {
        private const val PIN_LENGTH = 100
    }
}

@Stable
data class PinConfigUiState(
    val pin: String = "",
    val repeatPin: String = "",
    val isLoading: IsLoadingState = IsLoadingState.NotLoading,
    val validationErrors: ImmutableSet<PinConfigValidationErrors> = persistentSetOf()
)

enum class PinConfigValidationErrors {
    PinBlank,
    PinDoesNotMatch
}
