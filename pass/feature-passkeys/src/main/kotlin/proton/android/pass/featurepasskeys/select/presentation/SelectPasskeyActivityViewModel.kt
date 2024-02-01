/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.featurepasskeys.select.presentation

import androidx.compose.runtime.Immutable
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.provider.CallingAppInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.some
import proton.android.pass.log.api.PassLogger
import proton.android.pass.passkeys.api.AuthenticateWithPasskey
import proton.android.pass.passkeys.api.PasskeyManager
import javax.inject.Inject

data class SelectPasskeyRequest(
    val callingAppInfo: CallingAppInfo,
    val callingRequest: GetPublicKeyCredentialOption,
    val passkeyId: String
)

@Immutable
sealed interface State {

    @Immutable
    object Idle : State

    @Immutable
    @JvmInline
    value class SendResponse(val response: String) : State

    @Immutable
    object NoPasskeyFound : State
}

@HiltViewModel
class SelectPasskeyActivityViewModel @Inject constructor(
    private val authenticateWithPasskey: AuthenticateWithPasskey,
    private val passkeyManager: PasskeyManager
) : ViewModel() {

    private var request: Option<SelectPasskeyRequest> = None

    private val _state: MutableStateFlow<State> = MutableStateFlow(State.Idle)
    val state: StateFlow<State> = _state

    fun setRequest(request: SelectPasskeyRequest) {
        this.request = request.some()
    }

    fun onButtonClick() = viewModelScope.launch {
        val requestValue = request.value() ?: return@launch
        val origin = requestValue.callingAppInfo.origin ?: run {
            PassLogger.w(TAG, "requestValue.callingRequest.origin was null")
            _state.update { State.NoPasskeyFound }
            return@launch
        }
        val passkey = passkeyManager.getPasskeyById(requestValue.passkeyId).value() ?: run {
            PassLogger.w(TAG, "Passkey with id ${requestValue.passkeyId} was not found")
            _state.update { State.NoPasskeyFound }
            return@launch
        }

        val response = authenticateWithPasskey(origin, passkey, requestValue.callingRequest.requestJson)
        PassLogger.d(TAG, "Generated Passkey authentication response")

        _state.update { State.SendResponse(response.response) }
    }

    fun clearPasskeys() = viewModelScope.launch {
        passkeyManager.clearPasskeys()
    }

    companion object {
        private const val TAG = "SelectPasskeyActivityViewModel"
    }
}
