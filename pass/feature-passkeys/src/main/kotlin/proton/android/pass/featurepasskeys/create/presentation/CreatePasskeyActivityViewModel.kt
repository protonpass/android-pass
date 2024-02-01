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

package proton.android.pass.featurepasskeys.create.presentation

import androidx.compose.runtime.Immutable
import androidx.credentials.CreatePublicKeyCredentialRequest
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
import proton.android.pass.passkeys.api.GeneratePasskey
import proton.android.pass.passkeys.api.PasskeyManager
import javax.inject.Inject

data class CreatePasskeyRequest(
    val callingAppInfo: CallingAppInfo,
    val callingRequest: CreatePublicKeyCredentialRequest,
)

@Immutable
sealed interface State {

    @Immutable
    object Idle : State

    @Immutable
    object Close : State

    @Immutable
    @JvmInline
    value class SendResponse(val response: String) : State
}

@HiltViewModel
class CreatePasskeyActivityViewModel @Inject constructor(
    private val passkeyManager: PasskeyManager,
    private val generatePasskey: GeneratePasskey
) : ViewModel() {

    private var request: Option<CreatePasskeyRequest> = None

    private val _state: MutableStateFlow<State> = MutableStateFlow(State.Idle)
    val state: StateFlow<State> = _state

    fun setRequest(request: CreatePasskeyRequest) {
        this.request = request.some()
    }

    fun onButtonClick() = viewModelScope.launch {
        val requestValue = request.value() ?: return@launch
        val origin = requestValue.callingRequest.origin ?: run {
            PassLogger.w(TAG, "requestValue.callingRequest.origin was null")
            _state.update { State.Close }
            return@launch
        }

        val created = generatePasskey(origin, requestValue.callingRequest.requestJson)
        PassLogger.i(TAG, "Created passkey")
        PassLogger.d(
            TAG,
            "rpname=${created.rpName} userDisplayName=${created.userDisplayName} username=${created.userName}"
        )
        passkeyManager.storePasskey(
            content = created.passkey,
            domain = requestValue.callingRequest.origin.toString(),
            identity = created.userDisplayName
        )
        _state.update { State.SendResponse(created.response) }
    }

    fun clearPasskeys() = viewModelScope.launch {
        passkeyManager.clearPasskeys()
    }

    companion object {
        private const val TAG = "CreatePasskeyActivityViewModel"
    }
}
