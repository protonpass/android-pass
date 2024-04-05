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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.some
import proton.android.pass.data.api.usecases.passkeys.GetPasskeyById
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.PasskeyId
import proton.android.pass.domain.ShareId
import proton.android.pass.featurepasskeys.telemetry.AuthDone
import proton.android.pass.log.api.PassLogger
import proton.android.pass.passkeys.api.AuthenticateWithPasskey
import proton.android.pass.preferences.HasAuthenticated
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

data class UsePasskeyNoUiRequest(
    val origin: String,
    val requestJson: String,
    val shareId: ShareId,
    val itemId: ItemId,
    val passkeyId: PasskeyId,
    val clientDataHash: ByteArray
)

sealed interface UsePasskeyState {
    data object Idle : UsePasskeyState
    data object Cancel : UsePasskeyState

    @JvmInline
    value class SendResponse(val response: String) : UsePasskeyState
}

@HiltViewModel
class UsePasskeyNoUiViewModel @Inject constructor(
    private val authenticateWithPasskey: AuthenticateWithPasskey,
    private val getPasskeyById: GetPasskeyById,
    private val telemetryManager: TelemetryManager,
    private val preferenceRepository: UserPreferencesRepository
) : ViewModel() {

    private val requestFlow: MutableStateFlow<Option<UsePasskeyNoUiRequest>> =
        MutableStateFlow(None)

    val state: StateFlow<UsePasskeyState> = requestFlow.mapLatest { requestOption ->
        val request = requestOption.value() ?: return@mapLatest UsePasskeyState.Idle

        val response = resolveChallenge(request).getOrElse {
            PassLogger.w(TAG, "Error resolving challenge")
            PassLogger.w(TAG, it)
            return@mapLatest UsePasskeyState.Cancel
        }

        UsePasskeyState.SendResponse(response)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = UsePasskeyState.Idle
    )

    fun setRequest(request: UsePasskeyNoUiRequest) {
        requestFlow.update { request.some() }
    }

    fun onStop() = viewModelScope.launch {
        preferenceRepository.setHasAuthenticated(HasAuthenticated.NotAuthenticated)
    }

    private suspend fun resolveChallenge(request: UsePasskeyNoUiRequest): Result<String> = runCatching {
        val passkey = getPasskeyById(
            shareId = request.shareId,
            itemId = request.itemId,
            passkeyId = request.passkeyId
        )

        when (passkey) {
            None -> throw IllegalStateException("Passkey not found")
            is Some -> {
                val response = authenticateWithPasskey(
                    origin = request.origin,
                    passkey = passkey.value,
                    requestJson = request.requestJson,
                    clientDataHash = request.clientDataHash
                )
                response.response
            }
        }
    }.onSuccess {
        telemetryManager.sendEvent(AuthDone)
    }

    companion object {
        private const val TAG = "UsePasskeyNoUiViewModel"
    }
}
