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

package proton.android.pass.features.credentials.passwords.usage.presentation

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
import proton.android.pass.common.api.some
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.preferences.HasAuthenticated
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject

@HiltViewModel
internal class PasswordCredentialUsageViewModel @Inject constructor(
    private val encryptionContextProvider: EncryptionContextProvider,
    private val userPreferenceRepository: UserPreferencesRepository
) : ViewModel() {

    private val requestOptionFlow = MutableStateFlow<Option<PasswordCredentialUsageRequest?>>(
        value = None
    )

    internal val stateFlow: StateFlow<PasswordCredentialUsageState> = requestOptionFlow
        .mapLatest { requestOption ->
            when (requestOption) {
                None -> PasswordCredentialUsageState.NotReady
                is Some -> requestOption.value?.let { request ->
                    encryptionContextProvider.withEncryptionContextSuspendable {
                        decrypt(request.encryptedPassword)
                    }.let { password ->
                        PasswordCredentialUsageState.Ready(
                            id = request.username,
                            password = password
                        )
                    }
                } ?: PasswordCredentialUsageState.Cancel
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PasswordCredentialUsageState.NotReady
        )

    internal fun onUpdateRequest(newRequest: PasswordCredentialUsageRequest?) {
        requestOptionFlow.update { newRequest.some() }
    }

    internal fun onStop() {
        userPreferenceRepository.setHasAuthenticated(HasAuthenticated.NotAuthenticated)
    }

}
