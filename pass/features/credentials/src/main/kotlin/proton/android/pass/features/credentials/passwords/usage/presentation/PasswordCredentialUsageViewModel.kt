/*
 * Copyright (c) 2025-2026 Proton AG
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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.HasActiveAccount
import proton.android.pass.features.credentials.R
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.ToastManager
import proton.android.pass.preferences.HasAuthenticated
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject

@HiltViewModel
internal class PasswordCredentialUsageViewModel @Inject constructor(
    private val encryptionContextProvider: EncryptionContextProvider,
    private val hasActiveAccount: HasActiveAccount,
    private val userPreferenceRepository: UserPreferencesRepository,
    private val toastManager: ToastManager
) : ViewModel() {

    private val mutableStateFlow =
        MutableStateFlow<PasswordCredentialUsageState>(PasswordCredentialUsageState.NotReady)

    internal val stateFlow: StateFlow<PasswordCredentialUsageState> = mutableStateFlow

    internal fun onUpdateRequest(newRequest: PasswordCredentialUsageRequest?) {
        if (newRequest == null) {
            PassLogger.w(TAG, "Password credential usage request is null, cancelling")
            mutableStateFlow.update { PasswordCredentialUsageState.Cancel }
            return
        }

        viewModelScope.launch {
            if (!hasActiveAccount(newRequest.userId)) {
                PassLogger.w(
                    TAG,
                    "Denying stale password credential usage request: no active account for userId"
                )
                mutableStateFlow.update { PasswordCredentialUsageState.Cancel }
                return@launch
            }

            val password = encryptionContextProvider.withEncryptionContextSuspendable {
                decrypt(newRequest.encryptedPassword)
            }
            if (password.isEmpty()) {
                PassLogger.w(TAG, "Decrypted password is empty, cannot create PasswordCredential")
                toastManager.showToast(R.string.password_credential_selection_empty_password_error)
                mutableStateFlow.update { PasswordCredentialUsageState.Cancel }
                return@launch
            }

            mutableStateFlow.update {
                PasswordCredentialUsageState.Ready(
                    id = newRequest.username,
                    password = password
                )
            }
        }
    }

    internal fun onStop() {
        userPreferenceRepository.setHasAuthenticated(HasAuthenticated.NotAuthenticated)
    }

    private companion object {

        private const val TAG = "PasswordCredentialUsageViewModel"

    }

}
