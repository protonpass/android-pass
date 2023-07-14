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
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.CommonRegex.NON_DIGIT_REGEX
import proton.android.pass.data.api.usecases.CreatePin
import proton.android.pass.featureprofile.impl.ProfileSnackbarMessage.PinLockEnabled
import proton.android.pass.featureprofile.impl.pinconfig.PinConfigValidationErrors.PinBlank
import proton.android.pass.featureprofile.impl.pinconfig.PinConfigValidationErrors.PinDoesNotMatch
import proton.android.pass.featureprofile.impl.pinconfig.PinConfigValidationErrors.PinTooShort
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.AppLockTypePreference
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class PinConfigViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val createPin: CreatePin,
    private val snackbarDispatcher: SnackbarDispatcher
) : ViewModel() {

    private val _state: MutableStateFlow<PinConfigUiState> = MutableStateFlow(PinConfigUiState())
    val state: StateFlow<PinConfigUiState> = _state

    fun onEnterPin(value: String) {
        _state.update { it.copy(validationErrors = persistentSetOf()) }
        val sanitisedValue = value.replace(NON_DIGIT_REGEX, "").take(MAX_PIN_LENGTH)
        _state.update { it.copy(pin = sanitisedValue) }
    }

    fun onRepeatPin(value: String) {
        _state.update { it.copy(validationErrors = persistentSetOf()) }
        val sanitisedValue = value.replace(NON_DIGIT_REGEX, "").take(MAX_PIN_LENGTH)
        _state.update { it.copy(repeatPin = sanitisedValue) }
    }

    fun onSubmit() {
        val currentState = _state.value
        if (currentState.pin.isBlank()) {
            _state.update { it.copy(validationErrors = persistentSetOf(PinBlank)) }
        } else if (currentState.pin.length < MIN_PIN_LENGTH) {
            _state.update { it.copy(validationErrors = persistentSetOf(PinTooShort)) }
        } else if (currentState.pin != currentState.repeatPin) {
            _state.update { it.copy(validationErrors = persistentSetOf(PinDoesNotMatch)) }
        } else {
            _state.update { it.copy(validationErrors = persistentSetOf()) }
            viewModelScope.launch {
                runCatching {
                    createPin(currentState.pin.encodeToByteArray())
                }.onSuccess {
                    userPreferencesRepository.setAppLockTypePreference(AppLockTypePreference.Pin)
                        .onSuccess {
                            _state.update { it.copy(event = PinConfigEvent.PinSet) }
                            snackbarDispatcher(PinLockEnabled)
                        }
                        .onFailure { PassLogger.e(TAG, it, "Failed to save app lock type") }
                    PassLogger.i(TAG, "Pin set successfully")
                }.onFailure {
                    PassLogger.e(TAG, it, "Failed to set pin")
                }
            }
        }
    }

    fun clearEvents() {
        _state.update { it.copy(event = PinConfigEvent.Unknown) }
    }

    companion object {
        private const val MAX_PIN_LENGTH = 100
        private const val MIN_PIN_LENGTH = 4
        private const val TAG = "PinConfigViewModel"
    }
}

sealed interface PinConfigEvent {
    object PinSet : PinConfigEvent
    object Unknown : PinConfigEvent
}

@Stable
data class PinConfigUiState(
    val pin: String = "",
    val repeatPin: String = "",
    val validationErrors: ImmutableSet<PinConfigValidationErrors> = persistentSetOf(),
    val event: PinConfigEvent = PinConfigEvent.Unknown
)

enum class PinConfigValidationErrors {
    PinBlank,
    PinTooShort,
    PinDoesNotMatch
}
