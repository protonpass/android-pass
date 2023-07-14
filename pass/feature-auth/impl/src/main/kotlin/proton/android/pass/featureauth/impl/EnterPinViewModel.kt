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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.biometry.StoreAuthSuccessful
import proton.android.pass.common.api.CommonRegex
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.some
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.CheckPin
import proton.android.pass.featureauth.impl.EnterPinUiState.NotInitialised
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.InternalSettingsRepository
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class EnterPinViewModel @Inject constructor(
    private val checkPin: CheckPin,
    private val storeAuthSuccessful: StoreAuthSuccessful,
    private val internalSettingsRepository: InternalSettingsRepository
) : ViewModel() {

    private val isLoading: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)
    private val pinErrorState: MutableStateFlow<Option<PinError>> = MutableStateFlow(None)
    private val eventState: MutableStateFlow<EnterPinEvent> =
        MutableStateFlow(EnterPinEvent.Unknown)
    private val pinState: MutableStateFlow<String> = MutableStateFlow("")
    val state: StateFlow<EnterPinUiState> = combine(
        eventState,
        pinState,
        pinErrorState,
        isLoading,
        EnterPinUiState::Data
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = NotInitialised
    )

    fun onPinChanged(value: String) {
        val sanitisedValue = value.replace(CommonRegex.NON_DIGIT_REGEX, "").take(MAX_PIN_LENGTH)
        pinErrorState.update { None }
        pinState.update { sanitisedValue }
    }

    fun onPinSubmit() = viewModelScope.launch {
        val pin = pinState.value
        if (pin.isEmpty()) {
            pinErrorState.update { PinError.PinEmpty.some() }
            return@launch
        }

        runCatching {
            val isMatch = checkPin(pinState.value.encodeToByteArray())
            if (isMatch) {
                storeAuthSuccessful()
                eventState.update { EnterPinEvent.Success }
            } else {
                isLoading.update { IsLoadingState.Loading }
                delay(WRONG_PIN_DELAY_SECONDS)
                isLoading.update { IsLoadingState.NotLoading }
                val attemptsCount = internalSettingsRepository.getPinAttemptsCount().first() + 1
                internalSettingsRepository.setPinAttemptsCount(attemptsCount)
                val remainingAttempts = MAX_PIN_ATTEMPTS - attemptsCount
                if (remainingAttempts <= 0) {
                    PassLogger.w(TAG, "Too many wrong attempts, logging user out")
                    eventState.update { EnterPinEvent.ForceSignOut }
                } else {
                    pinErrorState.update { PinError.PinIncorrect(remainingAttempts).some() }
                }
            }
        }.onFailure { PassLogger.d(TAG, it, "Failed to check pin") }
    }

    companion object {
        private val WRONG_PIN_DELAY_SECONDS = 2.seconds
        private const val MAX_PIN_ATTEMPTS = 5
        private const val MAX_PIN_LENGTH = 100
        private const val TAG = "EnterPinViewModel"
    }
}

sealed interface EnterPinEvent {
    object Success : EnterPinEvent
    object ForceSignOut : EnterPinEvent
    object Unknown : EnterPinEvent
}
