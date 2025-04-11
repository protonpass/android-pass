/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.credentials.passkeys.usage.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.toOption
import proton.android.pass.data.api.usecases.passkeys.GetPasskeyById
import proton.android.pass.features.credentials.shared.passkeys.events.PasskeyCredentialsTelemetryEvent
import proton.android.pass.log.api.PassLogger
import proton.android.pass.passkeys.api.AuthenticateWithPasskey
import proton.android.pass.preferences.HasAuthenticated
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

@HiltViewModel
internal class PasskeyCredentialUsageViewModel @Inject constructor(
    private val authenticateWithPasskey: AuthenticateWithPasskey,
    private val getPasskeyById: GetPasskeyById,
    private val preferenceRepository: UserPreferencesRepository,
    private val telemetryManager: TelemetryManager
) : ViewModel() {

    private val requestOptionFlow = MutableStateFlow<Option<PasskeyCredentialUsageRequest>>(
        value = None
    )

    internal val stateFlow: StateFlow<PasskeyCredentialUsageState> = requestOptionFlow.mapLatest { requestOption ->
        val request = requestOption.value() ?: return@mapLatest PasskeyCredentialUsageState.NotReady

        val response = resolveChallenge(request).getOrElse { error ->
            PassLogger.w(TAG, "Error resolving Passkey challenge")
            PassLogger.w(TAG, error)
            return@mapLatest PasskeyCredentialUsageState.Cancel
        }

        PasskeyCredentialUsageState.Ready(authResponseJson = response)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = PasskeyCredentialUsageState.NotReady
    )

    private suspend fun resolveChallenge(request: PasskeyCredentialUsageRequest): Result<String> = runCatching {
        getPasskeyById(
            shareId = request.shareId,
            itemId = request.itemId,
            passkeyId = request.passkeyId
        ).let { passkey ->
            when (passkey) {
                None -> throw IllegalStateException("Passkey not found")
                is Some -> {
                    authenticateWithPasskey(
                        passkey = passkey.value,
                        origin = request.requestOrigin,
                        requestJson = request.requestJson,
                        clientDataHash = request.clientDataHash
                    ).response
                }
            }
        }
    }.onSuccess {
        telemetryManager.sendEvent(PasskeyCredentialsTelemetryEvent.AuthDone)
    }

    internal fun onUpdateRequest(newRequest: PasskeyCredentialUsageRequest?) {
        requestOptionFlow.update { newRequest.toOption() }
    }

    internal fun onStop() {
        preferenceRepository.setHasAuthenticated(HasAuthenticated.NotAuthenticated)
    }

    private companion object {

        private const val TAG = "PasskeyCredentialUsageViewModel"

    }

}
