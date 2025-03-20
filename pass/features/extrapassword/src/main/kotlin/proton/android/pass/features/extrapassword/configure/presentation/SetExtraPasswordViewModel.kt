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

package proton.android.pass.features.extrapassword.configure.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import javax.inject.Inject

@HiltViewModel
class SetExtraPasswordViewModel @Inject constructor(
    private val encryptionContextProvider: EncryptionContextProvider,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    @OptIn(SavedStateHandleSaveableApi::class)
    private var mutableExtraPasswordState: SetExtraPasswordState by savedStateHandleProvider.get()
        .saveable { mutableStateOf(SetExtraPasswordState.EMPTY) }

    private val eventFlow: MutableStateFlow<SetExtraPasswordEvent> =
        MutableStateFlow(SetExtraPasswordEvent.Idle)
    private val validationErrorFlow: MutableStateFlow<Option<SetExtraPasswordValidationErrors>> =
        MutableStateFlow(None)

    val state: StateFlow<SetExtraPasswordNameUiState> = combine(
        eventFlow,
        validationErrorFlow,
        ::SetExtraPasswordNameUiState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SetExtraPasswordNameUiState.Initial
    )

    internal fun getExtraPasswordState(): SetExtraPasswordState = mutableExtraPasswordState

    internal fun onExtraPasswordValueChanged(value: String) {
        val password = value.sanitise()
        validationErrorFlow.update { None }
        mutableExtraPasswordState = mutableExtraPasswordState.copy(password = password)
    }

    internal fun onExtraPasswordRepeatValueChanged(value: String) {
        val password = value.sanitise()
        validationErrorFlow.update { None }
        mutableExtraPasswordState = mutableExtraPasswordState.copy(repeatPassword = password)
    }

    private fun String.sanitise() = replace("\n", "")

    internal fun submit() {
        val password = mutableExtraPasswordState.password
        if (password.isBlank()) {
            validationErrorFlow.update { Some(SetExtraPasswordValidationErrors.BlankPassword) }
            return
        }
        if (password.length < MIN_PASSWORD_LENGTH) {
            validationErrorFlow.update { Some(SetExtraPasswordValidationErrors.MinimumLengthPassword) }
            return
        }
        if (password != mutableExtraPasswordState.repeatPassword) {
            validationErrorFlow.update { Some(SetExtraPasswordValidationErrors.PasswordMismatch) }
            return
        }
        encryptionContextProvider.withEncryptionContext {
            eventFlow.update { SetExtraPasswordEvent.Success(encrypt(password)) }
        }
    }

    internal fun onEventConsumed(event: SetExtraPasswordEvent) {
        eventFlow.compareAndSet(event, SetExtraPasswordEvent.Idle)
    }

    companion object {
        private const val MIN_PASSWORD_LENGTH = 8
    }
}


